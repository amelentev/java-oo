This eclipse plugin uses AspectJ aspects and Equinox weaving to modify Eclipse Java Compiler for operator overloading support.

If something doesn't work:

1. Ensure `org.eclipse.equinox.weaving.aspectj` bundle is auto-started at level 2. Look at `eclipse/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info`:

```
org.eclipse.equinox.weaving.aspectj,1.0.300.I20130319-1000,plugins/org.eclipse.equinox.weaving.aspectj_1.0.300.I20130319-1000.jar,2,true
```

2. Ensure the following bundles are installed:

```
org.aspectj.weaver
org.eclipse.equinox.weaving.aspectj
org.eclipse.equinox.weaving.caching
org.eclipse.equinox.weaving.hook
eclipse-oo-plugin
```

3. Check org.eclipse.equinox.weaving.hook is added to osgi extensions (`eclipse/configuration/config.ini`):

```
osgi.framework.extensions=reference\:file\:org.eclipse.equinox.weaving.hook_1.0.200.I20130319-1000.jar
```

4. Add following to JVM argumets (after `-vmargs` in `eclipse/eclipse.ini`) to enable weaving debug info:

```
-Daj.weaving.verbose=true
-Dorg.aspectj.weaver.showWeaveInfo=true
-Dorg.aspectj.osgi.verbose=true
```

See also http://wiki.eclipse.org/Equinox_Weaving_QuickStart
