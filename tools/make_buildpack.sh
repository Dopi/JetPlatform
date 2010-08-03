#/bin/bash
#check if we are in the right working directory
if [ -f make_buildpack.sh ]; then 
cd ..
fi 

#pack the build-archive
cd ..
tar --owner=root --group=root -czvf JetPlatform-build+patch-$(date +%y%m%d).tgz JetPlatform/build.sh JetPlatform/delta JetPlatform/patch JetPlatform/tools android
cd JetPlatform
