# Android Architecture Components Note

Google 在 Google I/O 2017 发布的 Android Architecture Components，包括 Lifecycle，LiveData，ViewModel 以及 Room 组成。

## References

1. [使用 Lifecycle 处理生命周期](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0524/7969.html)

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
