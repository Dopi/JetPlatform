#/bin/sh
#check if the source directory exists
if [ -d source ]; then
echo "Copying files from delta directory ..."
cp -rf delta/* source/
echo "Applying patches ..."
# apply patches from patch directory
echo "Starting build ..."
cd source
make -i -j4 PRODUCT-sec_GT-S8000-user otapackage dist
else
echo "source directory does not exist. Please use tools/get_source.sh to fetch the source."
fi

