for f in `ls ../NoidAccessibility/TransDroid/test-guidelines/*.json`; do
	echo $f
	./run-talkPP.sh $f
done
