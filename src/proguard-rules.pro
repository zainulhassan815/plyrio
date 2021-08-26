# Add any ProGuard configurations specific to this
# extension here.

-keep public class com.dreamers.plyr.Plyr {
    public *;
}
-keep public class com.dreamers.plyr.Plyr$JSInterface {
     public *;
}

-keepclassmembers class com.dreamers.plyr.Plyr$JSInterface {
      <methods>;
}

-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses com/dreamers/plyr/repack
-flattenpackagehierarchy
-dontpreverify

-keepattributes JavascriptInterface
-keepattributes *Annotation*
