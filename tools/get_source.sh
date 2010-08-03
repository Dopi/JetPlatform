#/bin/bash
echo "This script will need approx 5GB harddisk space. It this is not available it will fail."

#check if we are in the right working directory
if [ -f get_source.sh ]; then 
cd ..
fi 

#check if the source files were already downloaded
if [ -d tmp ]; then
echo "tmp directory exists. If it does not contain all source files delete the directory or get the missing files manually."
else
mkdir tmp
cd tmp
echo "Getting source files ..."
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p00
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p01
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p02
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p03
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p04
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p05
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p06
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p07
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p08
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p09
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p10
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p11
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p12
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p13
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p14
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p15
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p16
wget http://jetdroid.googlecode.com/files/JetPlatform-source-2.1.tar.lzma.p17
cd ..
fi

#unpack the source
mkdir source
cd source
echo "Unpacking source ..."
cat ../tmp/JetPlatform-source-2.1.tar.lzma.p00 ../tmp/JetPlatform-source-2.1.tar.lzma.p01 ../tmp/JetPlatform-source-2.1.tar.lzma.p02 ../tmp/JetPlatform-source-2.1.tar.lzma.p03 ../tmp/JetPlatform-source-2.1.tar.lzma.p04 ../tmp/JetPlatform-source-2.1.tar.lzma.p05 ../tmp/JetPlatform-source-2.1.tar.lzma.p06 ../tmp/JetPlatform-source-2.1.tar.lzma.p07 ../tmp/JetPlatform-source-2.1.tar.lzma.p08 ../tmp/JetPlatform-source-2.1.tar.lzma.p09 ../tmp/JetPlatform-source-2.1.tar.lzma.p10 ../tmp/JetPlatform-source-2.1.tar.lzma.p11 ../tmp/JetPlatform-source-2.1.tar.lzma.p12 ../tmp/JetPlatform-source-2.1.tar.lzma.p13 ../tmp/JetPlatform-source-2.1.tar.lzma.p14 ../tmp/JetPlatform-source-2.1.tar.lzma.p15 ../tmp/JetPlatform-source-2.1.tar.lzma.p16 ../tmp/JetPlatform-source-2.1.tar.lzma.p17 | tar --lzma -x
cd ..

echo "Unpacking the source is complete. You may now delete the tmp directory."
