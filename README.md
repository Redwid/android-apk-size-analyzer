# android-apk-size-analyzer
What is doing: unzips resources, decompiles DEX and android manifest file and creates a tree of files counts and sizes per directory.

For DEX decompilation tool using this tool: https://sourceforge.net/projects/dex2jar/

For XML decompilation tool using this tool: AXMLPrinter2.

Both tools already included into the project

That tool won’t work on Windows PCs out of the box. 
Needs to modify usage of dex2jar script from sh to bat.

You could use it to find out how compiler converts your classes.

## Usage 

`java ApkSizeAnalyzer /path-to-your-apk-file.apk(*.aar)`

or

`sh gradlew run --args=/path-or-http-url-to-your-apk-file.apk(*.aar)`

The unzipped apk resources will be placed in project /tmp folder.


To decompile class file you could use JD-GUI app: http://jd.benow.ca/

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)