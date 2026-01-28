# Getting Started

## 1. Installation

HyVoltz is a **Platform Mod**. You do not shade it. You depend on it.

1.  Add HyVoltz to your project dependencies (e.g. `compileOnly` in Gradle if provided via a repository, or local jar).
2.  Ensure your `manifest.json` (or equivalent) declares a dependency on `hyvoltz`.

## 2. Using the API

The entry point for all interactions is `HyVoltzAPI`.

```java
import me.almana.hyvoltz.api.HyVoltzAPI;

// Check if HyVoltz is loaded (though it should be enforced by dependency)
try {
    HyVoltzAPI.engine();
} catch (IllegalStateException e) {
    // Handle missing dependency gracefully
}
```

## 3. Creating a Node

To make a block participate in the network, you use `HyVoltzNodeComponent`.

```java
public class MyGenerator extends HyVoltzNodeComponent implements ElectricProducer {
    public MyGenerator() {
         super(new SimpleProducerNode(10)); // 10 power/tick
    }
}
```

## 4. Lifecycle Management

You MUST manually attach and detach your nodes when they enter/leave the world. This is usually done in your Block or Entity Component's lifecycle methods. HyVoltz will not do this for you. If you move a block, you must detach -> move -> attach. 

If you move a block and do not detach the node, it will continue to exist in the network and will void energy. 

```java
@Override
public void onAttach() {
    // Register with the engine
    // REQUIRED: World UUID and accurate Block Position
    myGenerator.attach(entity.getWorld().getUuid(), entity.getPosition());
}

@Override
public void onDetach() {
    // Unregister
    myGenerator.detach(); 
}
```

> **Warning:** If you move a block (e.g. via strict block movement), you must detach -> move -> attach.

## 5. Ticking

Your nodes will automatically participate in the network simulation *if* the server is ticking. HyVoltz hooks into the server loop automatically. You do not need to manually tick your nodes for *network* logic, only for your own *gameplay* logic (e.g. burning coal).
 
