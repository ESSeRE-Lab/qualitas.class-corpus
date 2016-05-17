
# Comes from the Debian SGML/XML HOWTO

MAX_TEX_RECURSION=4

# path to XML declaration and stylesheet files: hard-coded in order
# that the Makefile runs in {build.doc}
# If you want to run it in a current directory, set correctly the
# following path
XML_DECL=../../../xml/docbook/declaration/xml.dcl
PRINT_SS=../../../xml/docbook/stylesheet/dsssl/ldp.dsl

%.tex: %.xml
	jade -t tex -V tex-backend \
		-d $(PRINT_SS) \
		$(XML_DECL) $<

%.dvi: %.tex $(PRINT_SS) $(XML_DECL)
	# Trick from Adam Di Carlo <adam@onshore.com> to recurse jadetex 
	# "just enough".
	-cp -pf prior.aux pprior.aux
	-cp -pf $(shell basename $< .tex).aux prior.aux
	jadetex $<
	if ! cmp $(shell basename $< .tex).aux prior.aux &&		\
	   ! cmp $(shell basename $< .tex).aux pprior.aux &&		\
	   expr $(MAKELEVEL) '<' $(MAX_TEX_RECURSION); then		\
		rm -f $@						;\
		$(MAKE) $@						;\
	fi
	rm -f prior.aux pprior.aux

%.ps: %.dvi
	dvips -f $< > $@

%.pdf: %.dvi
	dvipdf $< > $@
