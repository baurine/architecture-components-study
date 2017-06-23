# Android Architecture Components Note

Google 在 Google I/O 2017 发布的 Android Architecture Components，包括 Lifecycle，LiveData，ViewModel 以及 Room 组成。其中 Room 是独立的部分，其余三部分是有关联的，其中 Lifecycle 是基础，LiveData 用到 Lifecycle，而 ViewModel 是作为 LiveData 的容器，因此，它们逐层递进。个人觉得 LiveData 是这里稍微最复杂的部分。

## References

1. [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) (发现不少笔误...)
1. 中文翻译合集：
   1. [泡在网上的日子](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0613/8070.html)
   1. [掘金](https://juejin.im/post/5937b1d7a22b9d005810b877)

对于官网上的文档，正如上面所言，Lifecycle，LiveData，ViewModel 是逐层递进的关系，因此建议的阅读顺序：

1. Overview：简短的介绍视频和博客，先有个全局的大概认识，看不懂没关系
   1. [Video - Architecture Components: Improve Your App's Design](https://www.youtube.com/watch?time_continue=334&v=vOJCrbr144o)
   1. [Blog - Android and Architecture](https://android-developers.googleblog.com/2017/05/android-and-architecture.html)
1. Adding Components to your Project：先尝试新建一个工程，把这些库依赖上，主要是为了后面可以边看文章边看源码进行理解
1. Handling Lifecycle
1. LiveData
1. ViewModel
1. Room Persistence Libraray：这篇也可以放在 Handling Lifecycle 之前看，因为比较独立
1. 尝试 codelab 中的代码
1. Guide to App Architecture：最后再回来看这篇指南
1. 看 [googlesamples/android-architecture-components](https://github.com/googlesamples/android-architecture-components) 中的代码

## Note

### Adding Components to your Project

设置 google maven 及添加 Lifecycle、LiveData、ViewModel、Room 等的依赖，略。

### Room

source: [Room Persistence Library（官网文档翻译）](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0525/7971.html)

Room 是 google android 官方的 ORM，用来简化 SQLite 操作。先大致了解一下。

Room 有三个主要的组件：

- Database，表示整个数据库的实例，一般一个应用中只需要一个全局的 Database 实例。
- Entity，表示数据库中的一个表。对每一个 entity，都会创建一个表来持有这些 item。entity 中的每一个 field 都将被持久化到数据库。
- DAO，Database Access Object，DAO 是 Room 的主要组件，它负责从 Database 中增删改查 Entity。

它们之间的关系，正如上面所言，从 Database 中得到 DAO，DAO 从 database 中操作 Entity，包括 Insert / Delete / Update / Query。

一个简单的例子。定义 Entity，用 @Entity 注解 Entity 类。

    @Entity
    public class User {
      @PrimaryKey
      private int uid;

      @ColumnInfo(name = "first_name")
      private String firstName;

      @ColumnInfo(name = "last_name")
      private String lastName;

      // Getters and setters are ignored for brevity,
      // but they're required for Room to work.
    }

用 @Dao 注解定义 DAO 类，DAO 是抽象类，由 Room 在编译过程中生成实体类，就跟 Retrofit 中定义 API 接口类，Dagger2 中定义 Component 一样。

    @Dao
    public interface UserDao {
      @Query("SELECT * FROM user")
      List<User> getAll();

      @Query("SELECT * FROM user WHERE uid IN (:userIds)")
      List<User> loadAllByIds(int[] userIds);

      @Query("SELECT * FROM user WHERE first_name LIKE :first AND "
              + "last_name LIKE :last LIMIT 1")
      User findByName(String first, String last);

      @Insert
      void insertAll(User... users);

      @Delete
      void delete(User user);
    }

用 @Database 注解定义 Database 类，同样也是抽象类。(继续联想 Retrofit 和 Dagger2 的用法)。

    @Database(entities = {User.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {
      public abstract UserDao userDao();
    }

最后，如何使用，先得到 AppDatabase 的实例 (最好用单例模式，全局只需要一个实例)，然后从 AppDatabase 的实例中得到 useDao。

    AppDatabase db = Room.databaseBuilder(getApplicationContext(),
            AppDatabase.class, "database-name").build();
    UserDao userDao = db.userDao();

剩下就是这三个组件的一些常见用法，简单了解，需要时再详细看文档。

对于 Entity：

- 可以用 @Ingnore 忽略不需要持久化到数据库的属性
- 用 @Entity(tableName='table_name') 指定表名，而不使用默认的类名作为表名
- 用 @Index 添加索引
- 用 @ForeignKey 指定外键和关联
- @Embedded，嵌套对象，类似 GraphQL 中的 Fragment

对于 DAO：

- @Insert
- @Delete
- @Update
- @Query
- 向查询中传递参数：`@Query("SELECT * FROM user WHERE age > :minAge")`
- 返回子集，即非 Entity 对象，而是常规的 POJO 对象
- 可观察的查询：LiveData，可以监听数据库的变化，从而自动更新 UI (?? 是这么理解吧)
- 查询返回 RxJava2 的 Publisher 和 Flowable 对象 (暂时不理解)
- 查询中返回原始的 cursor 对象，不推荐使用
- 多表查询，支持 join

使用类型转换器，TypeConverter，把自定义的数据类型，转换成 Room 能够持久化的数据类型。

定义转换器：

    public class Converters {
      @TypeConverter
      public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
      }

      @TypeConverter
      public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
      }
    }

然后把 TypeConverter 通过 @TypeConverters 注解添加到 AppDatabase 类中。(为什么全靠注解??)

    @Database(entities = {User.java}, version = 1)
    @TypeConverters({Converter.class})
    public abstract class AppDatabase extends RoomDatabase {
      public abstract UserDao userDao();
    }

(想想 Databinding 的 @BindingConversion，是不是很想似，一样是注解，一样是静态方法，实现类型转换)

#### 数据库迁移

实现 Migration 类的 migrate 方法，在 migrate 方法中通过执行 sql 语句实现迁移，并将 Migration 实例注册到生成 Database 的 builder 中。

    Room.databaseBuilder(getApplicationContext(), MyDb.class, "database-name")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
      @Override
      public void migrate(SupportSQLiteDatabase database) {
          database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, "
                  + "`name` TEXT, PRIMARY KEY(`id`))");
      }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
      @Override
      public void migrate(SupportSQLiteDatabase database) {
          database.execSQL("ALTER TABLE Book "
                  + " ADD COLUMN pub_year INTEGER");
      }
    };

#### 测试迁移

略。

### Handling Lifecycle

source: [使用 Lifecycle 处理生命周期](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0524/7969.html) 这篇文章，大致明白了 Lifecycle 的原理和作用，我理解成这类似一种控制反转，一种观察者模式或者一种解耦。

三大类：

1. Lifecycle：核心，管理 LifecycleObserver 队列，接受生命周期组件(如 Activity/Fragment)的状态，并通知给各个 LifecycleObserver，在生命周期组件结束时，自动清除 LifecycleObserver。实现类 LifecycleRegistry。
1. LifecycleOwner：持有 Lifecycle 实例的类，一般由生命周期组件(如 Activity/Fragment)实现。派生类 LifecycleRegistryOwner，由 LifecycleActivity/LifecycleFragment 实现，它们内部持有 LifecycleRegistry 实例。
1. LifecycleObserver：注册到 Lifecycle 中的观察者，由各个业务实现。

在没有使用 Lifecycle 之前，我们把主要的逻辑都写在每个 Activity/Fragment 的生命周期函数中，每个 Activity/Fragment 的代码都会不一样，由 Activity/Fragment 主动去调用我们实现的这些类的方法，它们紧密地耦合在一起。比如示例中的代码：

    class MyActivity extends AppCompatActivity {
      private MyLocationListener myLocationListener;

      public void onCreate(...) {
        myLocationListener = new MyLocationListener(this, (location) -> {
          // update UI
        });
      }

      public void onStart() {
        super.onStart();
        myLocationListener.start();
      }

      public void onStop() {
        super.onStop();
        myLocationListener.stop();
      }
    }

使用 Lifecycle 后，Activity/Fragment 只需要在每个生命周期函数中给注册到其中的观察者发送当前处于的周期状态，其它的就不 care 了，它再也不用主动去执行我们定义的类的方法，而只是告诉它们现在我处于哪个生命周期，你们该干嘛就干嘛去，从而将主动权从 Activity/Fragment 转移到了各类自己手中 (从由中央统一管理转变为各诸侯自治)。而且每个 Activity/Fragment 的主要工作将变成在 onCreate 时注册不同的 LifecycleObserver，~~在 onDestroy 时移除 LifecycleObserver~~ (这个不需要 Activity/Fragment 自己主动去做，由后面会说到的 LifecycleRegistry 在 Activity/Fragment destroy 后自动移除 observer)，Activity/Fragment 的代码将变得异常简洁，而且所有的 Acitivty/Fragment 的逻辑都变得相似。

我想象的 LifecycleActivity 简略内部实现：

    class LifecycleActivity extends AppCompatActivity {
      List<LifecycleObserver> observers;

      public void addObserver(LifecycleObserver observer) {
        observers.add(observer);
      }

      public void onCreate(...) {
        super.onCreate();
        notifyState(ON_CREATE);
      }

      public void onStart() {
        super.onStart();
        notifyState(ON_START);
      }

      public void onStop() {
        super.onStop();
        notifyState(ON_STOP);
      }

      ...

      private void notifyState(state) {
        for (observer in observers) {
          observer.notify(state);
        }
      }
    }

LifecycleObserver 的实现类使用了注解，来避免使用 switch...case 在同一个函数中接收不同的生命周期状态值并执行不同的操作：

    public class MyObserver implements LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      public void onResume() {
      }
  
      @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
      public void onPause() {
      }
    }

LifecycleActivity 中的 observer 队列，包括遍历通知 observer 状态，实际并不由 LifecycleActivity 直接管理，而是由 LifecycleRegistry 管理，LifecycleActivity 在不同生命周期函数中，将状态值传给 LifecycleRegistry 去通知 observers.

    // 想象的实现代码
    public class LifecycleRegistry {
      List<LifecycleObserver> observers;

      public void addObserver(observer) {...}
      public void removeObserver(observer) {...}
      public void notifyState(state) {...}
    }

    public class LifecycleActivity extends AppCompatActivity {
      private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

      public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
      }

      public void onCreate(...) {
        super.onCreate();
        lifecycleRegistry.notifyState(ON_CREATE);
      }

      public void onStart() {
        super.onStart();
        lifecycleRegistry.notifyState(ON_START);
      }

      public void onStop() {
        super.onStop();
        lifecycleRegistry.notifyState(ON_STOP);
      }

      ...
    }

Lifecycle 的最佳实践：略，理解。

凡事有利有弊，使用 Lifecycle 后，可以大幅度简化 Activity/Fragment 的逻辑，根据代码守恒定律，LifecycleObserver 的代码将增加。假设我们以前写了一个 MusicPlayer 的类，用来控制音乐播放，因为这时候还没有 Lifecycle，所以它的代码并没有任何与生命周期相关的内容，这时，如果我们想改成用 Lifecycle，那么有 2 种做法：

1. 继承或改写 MusicPlayer 类，并实现 LifecyclerObserver 接口，增加生命周期的处理逻辑。

        public class LifecycleMusicPlayer extends MusicPlayer implement LifecycleObserver {
          @OnLifecycleEvent(Lifecycle.Event.ON_START)
          public void onStart() {
            startPlay();
          }

          ...
        }

1. 组合法，用一个 LifecycleObserver 包装原来的 MusicPlayer。

        public class LifecycleMusicPlayer implement LifecycleObserver {
          private MusicPlayer musicPlayer = new MusicPlayer();

          @OnLifecycleEvent(Lifecycle.Event.ON_START)
          public void onStart() {
            musicPlayer.startPlay();
          }
        }

之后再看一下 Lifecycle 的源码进行深入了解。

看了源码 (待补充：类之间的继承关系图)，跟我猜想的大致差不多，只是实现上复杂了很多，唯一疑惑的地方就是，在 LifeclcyeActivity 的实现中，并没有显式地在各个生命周期函数中操作 lifecycleRegistry，那它是怎么把当前的状态值传给 lifecycleRegistry，通知各个 observer 的呢? (难道这并不是真正的代码?)

    public class LifecycleActivity extends FragmentActivity implements LifecycleRegistryOwner {
        private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

        @Override
        public LifecycleRegistry getLifecycle() {
            return mRegistry;
        }
    }

### LiveData

(感觉和 Databinding 中的 ObservableField 有一点相似之处，对原始的 model 进行一层包装，但 LiveData 多了生命周期的处理)

疑问：

1. LiveData 和 MutableLiveData 的区别，从名字上后者是表示易变的，但从代码实现上看，没什么区别啊。

#### 基本理解

LiveData 和 LifecycleOwner 一起配合使用。LiveData 一般用单例模式，它可以向自身注册多个普通的 Observer，但每个注册的 observer 都必须和一个 LifecycleOwner 关联，每次注册观察者时，LiveData 会把 owner 和 observer 再打包成一个 LifecycleBoundObserver (LifecycleBoundObserver 是 LiveData 的内部类，所以它自然也引用了 LiveData)，然后把这个新的 observer 同时放入 LiveData 对象自身内部的观察者队列，以及 owner 内部的观察者队列中。(相当于新的 observer 同时注册到了 LiveData 和 LifecycleOwner 中，但不同的 observer 注册到的 LiveData 是同一个，LifecycleOwner 可以是多个，LiveData 和 LifecycleOwner 的关系是一对多)。

(待补一张图)

    // LiveData.java
    public void observe(LifecycleOwner owner, Observer<T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        // 将 owner, observer 打包成 LifecycleBoundObserver
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        // 放入 LiveData 自身观察者队列中
        LifecycleBoundObserver existing = mObservers.putIfAbsent(observer, wrapper);
        ...
        // 放入 owner 观察者队列中
        owner.getLifecycle().addObserver(wrapper);
        // 根据 owner 的状态更新 wrapper 的 active 值
        wrapper.activeStateChanged(isActiveState(owner.getLifecycle().getCurrentState()));
    }

因为 LifecycleBoundObserver 是一个 LifecycleObserver，所以它可以监听 owner 的生命周期变化，如果 owner destroy 了，它会从 LiveData 的观察者队列中移除，同时从 owner 的观察者队列中移除。

    // LifecycleBoundObserver in LiveData.java
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            removeObserver(observer);
            return;
        }
        // immediately set active state, so we'd never dispatch anything to inactive
        // owner
        activeStateChanged(isActiveState(owner.getLifecycle().getCurrentState()));
    }

    // LiveData.java
    public void removeObserver(final Observer<T> observer) {
        assertMainThread("removeObserver");
        // 从 LiveData 自身观察者队列移除
        LifecycleBoundObserver removed = mObservers.remove(observer);
        if (removed == null) {
            return;
        }
        // 从 owner 观察者队列移除
        removed.owner.getLifecycle().removeObserver(removed);
        removed.activeStateChanged(false);
    }

当 LifecycleBoundObserver 监听到 owner 的生命周期改变时，根据 owner 的状态修改自身的 active 值。当 active 值为 true 的 LifecycleBoundObserver 的数量由 1 变成 0 时，即所有 owner 都处于非活跃状态时，LiveData 会调用 onInActive() 方法，反之，从 0 变成 1 时，调用 onActive() 方法。onInAtive() 和 onActive() 方法由 LiveData 的派生类重写。在此文中，LiveData<Location> 在 onAtive() 中向系统服务 LocationManager 注册 MyLocationListener，监听 location 的变化，在 onInActive() 中取消监听 location。

LiveData 有三个最重要的方法：onActive()，onInActive()，setValue(value)，从上面我们已经了解了 onActive() 和 onInActive() 的使用。那么还剩下 setValue(value)。此文中，当 LiveData<Location> 在 onActive() 中监听 location 的变化，在 listener 的回调中得到 location 值，于是用 setValue(location) 保存 location 值，同时，setValue() 内部同时遍历所有的 LifecycleBoundObserver，将此值通知给所有处于活跃状态的 LifecycleBoundObserver 中原始的 Observer。LifecycleBoundObserver 的活跃状态体现了 LifecycleOwner 的活跃状态，由 LifecycleOwner 一般由 LifecycleActiviy/LifecycleFragment 实现，所以只有处于前台的 LifecycleActiviy/LifecycleFragment 才能收到原始的 Observer 的回调，在这些回调里的一般行为是更新 UI。

    // LiveData.java
    protected void setValue(T value) {
        ...
        // 保存此值
        mData = value;
        // 通知 LifecycleBoundObserver
        dispatchingValue(null);
    }

    private void dispatchingValue(@Nullable LifecycleBoundObserver initiator) {
      ...
                // 遍历通知 LifecycleBoundObserver
                for (Iterator<Map.Entry<Observer<T>, LifecycleBoundObserver>> iterator =
                        mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                    considerNotify(iterator.next().getValue());
                    ...
                }
      ...
    }

    private void considerNotify(LifecycleBoundObserver observer) {
        // 不通知处于非活跃状态的 LifecycleBoundObserver
        if (!observer.active) {
            return;
        }
        if (!isActiveState(observer.owner.getLifecycle().getCurrentState())) {
            return;
        }
        ...
        // 通知 LifecycleBoundObserver 所包裹的原始 Observer
        observer.observer.onChanged((T) mData);
    }

#### Transformations of LiveData

懂点 RxJava 的都能很容易理解这里的 Transformation，Transformation.map() 相当于 RxJava 中的 map()，而 Transformations.switchMap() 我理解成类似 RxJava 中的 flatMap()。

Transformations 用于 ViewModel，所以后面的内容等看完 ViewModel 再回来看。

看完 ViewModel 再回来看后面的内容，还是有点迷迷糊糊，似懂非懂，看看示例代码再说。

### ViewModel

从 ViewModel 的代码来看，它实际是个空壳子，关键在于实现它的派生类。你应该在它的派生类中存放 LiveData，所以它是 LiveData 的容器，这体现了它名字 ViewModel 的 Model 部分。而文章中也说了，虽然它的名字叫 ViewModel，但它实际并不持有 View 或 Context 的引用，由 ViewModel 中的 LiveData 直接和它们打交道 (实际上是和 LifecycleOwner 打交道，但 LifecycleOwner 一般是由 Activity 或 Fragment 实现的)。

ViewModel 的实现及使用：

    // ViewModel.java
    public abstract class ViewModel {
        protected void onCleared() {
        }
    }

    // 继承 ViewModel 实现自定义的 ViewModel，在其中存放 LiveData
    public class MyViewModel extends ViewModel {
        private MutableLiveData<List<User>> users;
        public LiveData<List<User>> getUsers() {
            if (users == null) {
                users = new MutableLiveData<List<Users>>();
                loadUsers();
            }
            return users;
        }

        private void loadUsers() {
            // do async operation to fetch users
        }
    }

    // 在 LifecycleOwner 中向 ViewModel 中的 LiveData 注册观察者 (这个示例代码有点小问题，应该继承自 LifecycleActivity，而不是 AppCompatActivity)
    public class MyActivity extends AppCompatActivity {
        public void onCreate(Bundle savedInstanceState) {
            // 通过 MyActivity 去取 MyViewModel 的实例，如果已经有了，直接获得，否则创建一个新实例
            MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);
            model.getUsers().observe(this, users -> {
                // update UI
            });
        }
    }

ViewModel 的主要作用，延长 data 的生命周期：

1. 解决 Activity 异常 destroy，re-create 后要重新获取数据的问题
1. 解决在同一个 Activity 中的多个 Fragment 间共享数据的问题

解决办法是，将 ViewModel 单例化，ViewModel 实例的生成不是直接 new 出来的，而是通过 ViewModelProviders 创建出来的，一旦创建出来后，就会保存在 ViewModelProviders 中，除非相应的 Activity 是正常地 finish 掉，否则这个 ViewModel 会在内存中一直存在，当 Activity 被 re-create 后，将能重新取到原来的 ViewModel，而 ViewModel 中存有之前的数据，这样，就不用再重新取一次数据了。(ViewModel 的 onCleared() 和 Activity 的 finish() 是怎么关联在一起的?)

疑问 (待研究)：

1. 如果我想在多个不同的 Activity 之间共享数据怎么办? (一种办法是直接用单例的 LiveData，一种办法是创建 ViewModel，给 ViewModelProviders.of() 参数传 ApplicationContext? 待研究)
1. 如果一个 app 中有多个相同 Activity 类的不同实例 (比如当前 activity 栈中有多个 UserProfileActivity，显示了不同人的信息)，我想在它们之间显示不同的数据，怎么办?

像上面这种场景，ViewModel 是不是就不适用了呢?

之前上面在讲 LiveData 时，一个 LiveData 可以用于多个 LifecycleOwner，所以 LiveData 一般用单例，而有了 ViewModel 后，ViewModel 的行为类似单例，因为它在一定程序上代替了 LiveData 的单例行为。

### Codelab

1. Lifecycle

   通过代码实践，掌握了其基本用法，ViewModel 里可以直接存放原始 model，不一定用来放 LiveData；LiveData 可以直接拿来用，不一定要实现派生类。
