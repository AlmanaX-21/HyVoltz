# FAQ

## Why are there no voltage tiers?
Voltage mechanics (like EU/t packets or High/Medium/Low voltage) add significant computational overhead (solving circuits) and player friction (exploding machines). HyVoltz aims for **simplicity and performance**. If you want tiers, you can implement them yourself by checking `output > limit` in your own code, but HyVoltz won't enforce it.

## Why aren't networks saved to disk?
Saving graphs is incredibly complex (handling unique IDs, partial chunk loading, file corruption). Runtime discovery is **robust**. It is self-healing. If a chunk corrupts, the network just reforms based on what's physically there. The library stays secured in case of issues with the world itself.

## Why isn't HyVoltz shaded?
HyVoltz is a **Platform Mod**. It manages a single, shared simulation for the entire world. If multiple mods shaded it, they would each run their own separate electricity networks that couldn't interact. By installing HyVoltz as a shared dependency, all mods can connect to the same grid.

## Can multiple mods share networks?
**Yes.** Because HyVoltz is a shared platform, a generator from Mod A can power a machine from Mod B seamlessly, as long as they are physically connected.

## Is client-side power supported?
No. HyVoltz is a **server-side only** simulation. You must sync values (like battery % or machine status) to the client yourself using Hytale's standard networking or entity data syncing.
