-dontobfuscate
-keepattributes LineNumberTable,SourceFile

-keepclassmembers class * extends org.wezerl.bfreader.io.WritableObject {
	*;
}

-keepclassmembers class * extends org.wezerl.bfreader.jsonwrap.JsonObject$JsonDeserializable {
	*;
}

-keepclassmembers class org.wezerl.bfreader.R { *; }
-keepclassmembers class org.wezerl.bfreader.R$xml {	*; }
-keepclassmembers class org.wezerl.bfreader.R$string {	*; }

-keepclassmembers class com.github.luben.zstd.* {
	*;
}
