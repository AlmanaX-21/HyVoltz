# Dependency & Platform Rules

HyVoltz is designed as a **Shared Platform Mod**. The purpose is to ensure a common API for all mods to follow.

## Runtime Enforcement

HyVoltz enforces its presence at runtime. You cannot "shade" (bundle) HyVoltz inside your own mod jar efficiently because it must manage a single global simulation for the world.

*   **HyVoltz is a Host**: It owns the `WorldEnergyManager`.
*   **One Version**: There should only be one version of HyVoltz installed on the server.

## Defining the Dependency

In your `manifest.json`, you should declare a dependency on `hyvoltz`.

### What happens if HyVoltz is missing?

If your mod tries to access `HyVoltzAPI` without the HyVoltz plugin loaded:

1.  **Startup**: Accessing checks like `HyVoltzAPI.engine()` will throw a clear `IllegalStateException`:
    > "HyVoltz is required but not loaded. Ensure 'hyvoltz' is installed."

2.  **Runtime**: Calls to `attachNode()` or `detachNode()` fail fast with the same error.

Your mod should fail gracefully or (preferably) not load at all if HyVoltz is missing.

## Shading

**Do NOT shade HyVoltz.**

If you shade HyVoltz, you create a private copy of the engine.
*   Your machines will NOT connect to other mods' machines.
*   Your mod will run a duplicate simulation, wasting CPU.
*   You will crash if multiple shaded copies try to register global event listeners.

Always simply expect `hyvoltz` to be present in the `mods/` folder.
