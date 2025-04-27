# TODO:

## In order of what I think is easiest -> hardest

1. Add logging 
    (I think this one won't be so bad, I made a dir for it called logs. You really just need to use PrintWriter). I littered the files with println statements that probably would be better off being logged to a file somewhere. 

2. Create better abstraction
    Some of the methods I have are pretty convoluted and could be a lot more digestible if we abstract a lot of things to make the logic a bit more forward. Don't make my mistake of abstracting TOO early though, I think wait a little on this. 

3. Handle utility failures
    Right now, if a utility server crashes the entire thing shits the bed.

4. Allow client to send sorting choice again
    Right now, Utility servers have to be instantiated with their sorting choice. I'm still not 100% if that's wrong or not, but it shouldn't be a crazy fix. The data for sorting still gets sent regardless.

5. Handle if more chunks than servers
    Basically have some sort of way to maintain chunks if servers are unavailable. Right now, if we have more chunks than servers, we basically just kind of ignore the extra chunks.

6. Create a system for ip pooling
    I've hardcoded three ports for the utility servers-- 32006, 32007, 32008. I don't really know how we'd go about dynamically adding / creating ports-- if there's some library for it I couldn't find it. To fix this you'd have to change both how the Master.java connects in the findAvailableUtilities() method. 

7. Dockerfiles / Kubernetes implementation
    This is a headache and I don't even want to think about it anymore.
