# Keep CyrillicRegistration and GreekRegistration for reflection
-keep class org.scilab.forge.jlatexmath.cyrillic.CyrillicRegistration { <init>(); }
-keep class org.scilab.forge.jlatexmath.greek.GreekRegistration { <init>(); }

# Keep TeXFormula constructors and methods for reflection
-keep class org.scilab.forge.jlatexmath.TeXFormula {
    <init>(...);
    public *;
}
# Keep MacroInfo constructors for reflection
-keep class org.scilab.forge.jlatexmath.MacroInfo {
    <init>(...);
}
# Keep TeXConstants fields for reflection
-keep class org.scilab.forge.jlatexmath.TeXConstants {
    *;
}
# Keep NewCommandMacro for reflection (used in MacroInfo)
-keep class org.scilab.forge.jlatexmath.NewCommandMacro {
    <init>();
    public java.lang.String executeMacro(...);
}
