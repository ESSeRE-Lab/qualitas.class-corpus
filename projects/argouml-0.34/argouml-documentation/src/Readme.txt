Welcome to ArgoUML!
-------------------

This is the ArgoUML Documentation README file. In it you will find basic 
information on how to build and use the ArgoUML documentation.


1. DESCRIPTION OF FILES

./
  .project              <<-- Project file for Eclipse.
  build.bat             <<-- Builds Docs from Windows command line.
  build.sh              <<-- Builds docs from *nix shell.
  build.xml             <<-- Ant build configuration file

build/*                 <<-- Where all of the output files go.

src/
  docbook-setup/*       <<-- Configuration files for the build process.
  manual/               <<-- Source code for the manual.
  quickguide/           <<-- Source code for the quickguide.
  *.launch              <<-- Config files for launching the build in Eclipse.
  Readme.txt            <<-- The file you are reading now!
  
src/manual/
  look-and-feel.css     <<-- Stylesheet for the html output.
  manual.xml            <<-- DocBook xml source for the user manual.

src/manual/images/*     <<-- Images used in the manual.

src/quickguide/
  look-and-feel.css     <<-- Stylesheet for the html output.
  declare-argostuff.ent <<-- Common definitions used in the quickguide.
  declare-authors.ent   <<-- List of authors.
  quickguide.xml        <<-- DocBook xml source base file for the quickguide.
  qstart-*.xml          <<-- DocBook xml source for the quickguide chapters.
  
src/quickguide/images/* <<-- Images used in the quick guide.

tools/
  apache-ant-x.x.x/*    <<-- Ant (the build tool).
  fop-x.x.x/*           <<-- Fop (the print formatter tool).
  
tools/lib/
  JimiProClasses.zip    <<-- You need to download this file! (See below).
  resolver.jar          <<-- Used to help locate the docbook catalog.
  saxon-x.x.x.jar       <<-- The stylesheet processor used during the build.

www/*                   <<-- The argouml-documentation project's website.
       

2. TO START THE BUILD

Windows:
> build.bat docs

Linux/Unix/Cygwin:
$ ./build.sh docs

Eclipse:
Click on project,
Run > External Tools > Open External Tools Dialogue...
Click on a configuration (e.g. "Documentation docs (build all)").
Click the Run button.

The generated documentation is all created within the "build" directory.  
The build process will generate this for you if it doesn't exist already.


3. BUILDING PDF

The documentation uses Fop (part of the Apache/XML project) to generate the
PDF. Fop relies on Sun's Jimi 1.0 library to process images in PNG format,
which cannot be distributed as part of ArgoUML for licensing reasons.

The version of Fop supplied will work fine, but any PNG images will be missing
from the final PDF.

If you wish to build the PDF with all images, you will have to fetch
you own version of Jimi. The included Fop file is prepared for Jimi.

(This information is from the Fop release notice 
 http://xml.apache.org/fop/relnotes.html)

1. Download Jimi (from http://java.sun.com/products/jimi/)

2. Extract the files from the archive

3. Copy JimiProClasses.zip to the tools/lib/ directory

You can then use the normal documentation build procedures for PDF, and PNG
images will be included.


4. CONTRIBUTING

If you wish to contribute to the manual or quickguide, please read the Cookbook
on how to work with the documentation.  We also invite you to subscribe to the 
dev@argouml-documentation.tigris.org mailing list, where you can contact the 
other authors to discuss your contributions.  More info at
http://argouml-documentation.tigris.org/servlets/ProjectMailingListList .

You are most welcome!


Have fun!
