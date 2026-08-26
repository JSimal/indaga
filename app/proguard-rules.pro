# kotlinx.serialization genera serializadores en tiempo de compilación que R8
# puede eliminar si no ve referencias directas a ellos. Mantenemos las clases
# de datos serializables y sus companions/serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.apkinves.toolbox.**.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.apkinves.toolbox.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.apkinves.toolbox.**$$serializer { *; }
