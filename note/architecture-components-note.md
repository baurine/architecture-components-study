# Android Architecture Components Note

Google 在 Google I/O 2017 发布的 Android Architecture Components，包括 Lifecycle，LiveData，ViewModel 以及 Room 组成。

## References

1. [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)
1. [使用 Lifecycle 处理生命周期](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0524/7969.html)
1. [Room Persistence Library（官网文档翻译）](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0525/7971.html)

## Note

### Lifecycle

看了 [使用 Lifecycle 处理生命周期](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0524/7969.html) 这篇文章，大致明白了 Lifecycle 的原理和作用，我理解成这类似一种控制反转，一种观察者模式或者一种解耦。

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

使用 Lifecycle 后，Activity/Fragment 只需要在每个生命周期函数中给注册到其中的观察者发送当前处于的周期状态，其它的就不 care 了，它再也不用主动去执行我们定义的类的方法，而只是告诉它们现在我处于哪个生命周期，你们该干嘛就干嘛去，从而将主动权从 Activity/Fragment 转移到了各类自己手中 (从由中央统一管理转变为各诸侯自治)。而且每个 Activity/Fragment 的主要工作将变成在 onCreate 时注册不同的 LifecycleObserver，在 onDestroy 时移除 LifecycleObserver，Activity/Fragment 的代码将变得异常简洁，而且所有的 Acitivty/Fragment 的逻辑都变得相似。

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
