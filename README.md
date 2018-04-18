# android-apk-size-analyzer
What is doing: unzips resources, decompiles DEX and android manifest file and creates a tree of files counts and sizes per directory.

For DEX decompilation tool using this: https://sourceforge.net/projects/dex2jar/

For XML decompilation tool using this: AXMLPrinter2.

Both tools already included into the project

That tool won’t work on Windows PCs out of the box. 
Needs to modify usage of dex2jar script from sh to bat.

You could use it to find out how compiler converts your classes.

### Usage 

java ApkSizeAnalyzer /path/to/your/apk/file.apk

In project /tmp folder app will unzip that application.

To decompile class file you could use JD-GUI app: 

http://jd.benow.ca/
