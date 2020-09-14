for f in `ls ../NoidAccessibility/TransDroid/test-guidelines/*.json`; do
	echo $f
	./run-with-mask.sh $f
done
