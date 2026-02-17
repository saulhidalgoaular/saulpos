LATEXMK := latexmk
ENGINE := lualatex
FLAGS := -$(ENGINE) -interaction=nonstopmode -halt-on-error -file-line-error

.PHONY: es en all clean

all: es en

es:
	mkdir -p build/es
	$(LATEXMK) -cd $(FLAGS) -output-directory=../build/es -jobname=saulpos-manual-es es/main.tex

en:
	mkdir -p build/en
	$(LATEXMK) -cd $(FLAGS) -output-directory=../build/en -jobname=saulpos-manual-en en/main.tex

clean:
	$(LATEXMK) -C -cd -output-directory=../build/es es/main.tex
	$(LATEXMK) -C -cd -output-directory=../build/en en/main.tex
	rm -rf build
