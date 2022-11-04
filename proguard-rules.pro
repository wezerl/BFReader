-dontobfuscate
-keepattributes LineNumberTable,SourceFile

-keepclassmembers class * extends com.wezerl.bfreader.io.WritableObject {
	*;
}

-keepclassmembers class * extends com.wezerl.bfreader.jsonwrap.JsonObject$JsonDeserializable {
	*;
}

-keepclassmembers class com.wezerl.bfreader.R { *; }
-keepclassmembers class com.wezerl.bfreader.R$xml {	*; }
-keepclassmembers class com.wezerl.bfreader.R$string {	*; }

-keepclassmembers class com.github.luben.zstd.* {
	*;
}
