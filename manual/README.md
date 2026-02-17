# SaulPOS Bilingual User Manual (LaTeX)

This project builds two professional book-style manuals:

- Spanish PDF: `build/es/saulpos-manual-es.pdf`
- English PDF: `build/en/saulpos-manual-en.pdf`

## Folder structure

```text
manual/
  es/main.tex
  en/main.tex
  shared/
    style.tex
    terms-es.tex
    terms-en.tex
    macros.tex
  images/
    logo/saul-technologies-logo.png
    placeholders/
      .gitkeep
  Makefile
  README.md
```

## Requirements

- TeX distribution with `lualatex` and `latexmk`.
- Common packages used in this project (`polyglossia`, `tcolorbox`, `booktabs`, etc.).

## Build on Linux/macOS

From repository root:

```bash
cd manual
make es
make en
make all
```

## Build on Windows (PowerShell)

Option A (with `make` available):

```powershell
cd manual
make es
make en
make all
```

Option B (without `make`, direct `latexmk`):

```powershell
cd manual
mkdir build\es -Force
latexmk -cd -lualatex -interaction=nonstopmode -halt-on-error -output-directory=../build/es -jobname=saulpos-manual-es es/main.tex
mkdir build\en -Force
latexmk -cd -lualatex -interaction=nonstopmode -halt-on-error -output-directory=../build/en -jobname=saulpos-manual-en en/main.tex
```

## Screenshot placeholder system

The manuals use `\SaulFigure{path}{caption}{label}`. This macro:

- Includes the real image if the file exists.
- Draws a framed placeholder if the image does not exist.
- Prints a machine-readable marker like `[FIGURE: ...]` under the caption.

This means both books compile before real screenshots are available.

## How to add real screenshots later (no chapter rewrite needed)

1. Keep the exact filenames from each manual's **Screenshot Checklist** appendix.
2. Add Spanish files under `manual/images/placeholders/es/`.
3. Add English files under `manual/images/placeholders/en/`.
4. Rebuild with `make es`, `make en`, or `make all`.

Only image files are replaced; chapter content remains unchanged.

## Logo

Cover pages reference:

- `manual/images/logo/saul-technologies-logo.png`

Replace this file with your official high-resolution logo to improve print quality.

## Clean artifacts

```bash
cd manual
make clean
```
