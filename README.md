# RxFlux
---

RxFlux是使用RxJava异步事件编程实现Flux模型的基础Android编程框架。RxFlux实现非业务相关，旨在传统MVC编程模型改造与整合Rxjava相关实现。
 
##背景
-----

经验上来说，一方面，老项目用的MVC框架存在以下问题：

1. View层，特别是复杂Activity页面，代码会堆积导致难于维护和不易于单元测试；

2. 事件触发点繁杂，难于跟踪调试。

**Flux vs MVC**:

1. Thinner view Layer, more readabel and testable;

2. Based on event, single direction flow make code clearer.

&emsp;&emsp;[Flux](https://facebook.github.io/flux/docs/overview.html)原本是Facebook推崇的Web端架构理念，而RxFlux是Flux的Android版实现。基于RxFlux的分层可以有效的解耦和减少View层代码，同时RxFlux的单向数据流也可以非常的方便事件跟踪。

![Flux模式结构][1]

另一方面，整合[Rxjava](https://github.com/ReactiveX/RxJava)的目的：

1. 利用Rxjava的异步编程模型和操作符简化异步代码编写；

2. 让代码更简洁化。

##路线图

------

- [x] Flux模型的基础实现

- [x] Flux模型的基础Demo

- [x] 用RxBus替换EventBus

- [ ] 比较Redux和ReFlux等类Flux模型的优缺点

- [ ] 整合Retrofit2,实现Rx网络请求

- [ ] 整合SqlBrite，实现Rx DAO

- [ ] 整合RxLifecycle，保证完整生命周期和避免内存泄漏

- [ ] 整合RxBinding，实现Rx UI

- [ ] 整合Dagger,实习DI

- [ ] APP Demo


##版权

------

&emsp;&emsp;**MIT**


  [1]: http://static.zybuluo.com/lean2/u3euu1vv7ex982nw66qbyc4o/image_1atq89tcd11j1ul2q821j4o1n50t.png
