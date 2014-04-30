% ============================================================================
%% aircc.cls V 1.1, 2012/09/13, (c) 2012 Thomas Zink
%%  minor modifications by Artem Melentyev, 2014.
%%
%% This is an unofficial Latex class for Authors of AIRCC Papers.
%% It tries to follow the formating guidelines set in the official
%% template "aircc_template.doc" as close as possible.
%% Unfortunately, some are not easily applicable in Latex. Examples include
%% text style combinations like \bf\sc\it which simply is generally not
%% supported. Some font sizes are also not directly supported.
%%
%% ***************************************************************************
%% Legal Notice:
%% This code is offered as-is without any warranty either expressed or
%% implied; without even the implied warranty of MERCHANTABILITY or
%% FITNESS FOR A PARTICULAR PURPOSE! 
%%
%% This work is distributed under the LaTeX Project Public License (LPPL)
%% ( http://www.latex-project.org/ ) version 1.3, and may be freely used,
%% distributed and modified. A copy of the LPPL, version 1.3, is included
%% in the base LaTeX documentation of all distributions of LaTeX released
%% 2003/12/01 or later.
%% Retain all contribution notices and credits.
%% ** Modified files should be clearly indicated as such, including  **
%% ** renaming them and changing author support contact information. **
%%
% ============================================================================
\ProvidesClass{aircc}[2012/06/14 V1.0 by Thomas Zink]
\typeout{-- Class for AIRCC Journal submissions --}
\NeedsTeXFormat{LaTeX2e}

\usepackage{rcs}
\RCS $Date: 2012/06/12 $
\RCS $Author: thomas zink (tzn) $
\RCS $file: aircc.cls $

\typeout{-- \RCSfile, Copyright (c) \RCSDate, by \RCSAuthor --}

% we can either use 'bold-extra':  \bf\sc
% or 'slantsc' and 'lmodern':      \sl\sc
% unfortunately, not both at the same time and no \bf\sl\sc
% we use bold-extra and find a workaround for Abstract and Keywords below
\usepackage{bold-extra} % allow \bf\sc

% ============================================================================
% 2.1. General Format, Page Layout and Margins
% ============================================================================

% Standard A4 (210mm x 297mm)
% left, right, top and bottom margins should be 30mm
%\usepackage[paper=a4paper,top=30mm,bottom=30mm,left=30mm,right=30mm]{geometry}
\usepackage[paper=a4paper,top=30mm,bottom=30mm,left=30mm,right=30mm, headsep=8pt, headheight=8pt, ignoreheadfoot]{geometry}

% no rule on header
\usepackage{fancyhdr}
\renewcommand{\headrulewidth}{0pt}

% Do not use any headers, footers or footnotes.  No page numbers.  Single column.  All main text paragraphs, including the abstract, must be fully (left and right) justified.  All text, including title, authors, headings, captions and body, will be Times New Roman font.
\pagestyle{empty}
\def\thepage{\arabic{page}}

\def\xsmall{\@setfontsize{\xsmall}{9}{10pt}}
\def\small{\@setfontsize{\small}{10}{12pt}}
\def\normalsize{\@setfontsize{\normalsize}{11}{13.3846pt}}%
\def\large{\@setfontsize{\large}{12}{14pt}}
\def\xlarge{\@setfontsize{\xlarge}{13}{14pt}}
\def\Large{\@setfontsize{\Large}{14}{17pt}}
\def\XLarge{\@setfontsize{\XLarge}{20}{22pt}}
\normalsize

% Do not indent paragraphs.
\setlength\parindent{0pt}
\setlength\parskip{6pt}
%\parfillskip=6pt

% ============================================================================
% 2.2. Title & 2.3 Authors
% ============================================================================
% The title is to be written in 20 pt. Garamond font, centred and using the bold and “Small Caps” formats. There should be 24 pt. (paragraph) spacing after the last line.

% Author names are to be written in 13 pt. Times New Roman format, centred and followed by a 12pt. paragraph spacing. If necessary, use superscripts to link individual authors with institutions as shown above.  Author affiliations are to be written in 12 pt. Times New Roman, centred, with email addresses, in 10 pt. Courier New, on the line following. The last email address will have an 18 pt. (paragraph) spacing following.

\newcommand{\@affiliation}{}
\def\affiliation#1{%
	\renewcommand{\@affiliation}{#1}%
}

\newcommand{\email}[1]{{\small #1}}

\def\maketitle{%
	\begin{center}
		{\XLarge\textbf{\textsc{\@title}}\\}
		\vspace{24pt}
		{\xlarge\@author\\}
		\vspace{12pt}
		{\large\@affiliation\\}
		\vspace{12pt} % 18 - parskip
	\end{center}
}

% ============================================================================
% 2.4. Abstract
% ============================================================================
% The Abstract section begins with the word, “Abstract” in 13 pt. Times New Roman, bold italics,
% “Small Caps” font with a 6pt. spacing following.  The abstract must not exceed 150 words in length in 10 pt.
% Times New Roman italics.  The text must be fully justified, with a 12 pt. paragraph spacing following the last line.

\newenvironment{abstract}
	%{\par\fontsize{13}{14}\textbf{\textit{\textsc{Abstract}}}\begin{itshape}\par\normalsize}
	{\par\fontsize{13}{14}\textbf{\textit{A}}\fontsize{10}{10}\textbf{\textit{BSTRACT}}\begin{itshape}\par\small}
	{\end{itshape}\vspace{6pt}}

% ============================================================================
% 2.5. Keywords
% ============================================================================
% The Keywords section begins with the word, “Keywords” in 13 pt. Times New Roman, bold italics,
% “Small Caps” font with a 6pt. spacing following.  There may be up to five keywords (or short phrases) separated by commas and six spaces,
% in 10 pt. Times New Roman italics.  An 18 pt. line spacing follows.

\newenvironment{keywords}
	%{\par\fontsize{13}{14}\textbf{\textsl{\textsc{Keywords}}}\begin{itshape}\par\small}
	{\par\fontsize{13}{14}\textbf{\textit{K}}\fontsize{10}{10}\textbf{\textit{EYWORDS}}\begin{itshape}\par\small}
	{\end{itshape}\vspace{12pt}}

% ============================================================================
%% 2.6. Section and sub-section headings
% ============================================================================
\newcounter{section}\setcounter{section}{0}
\newcounter{subsection}[section]
\newcounter{subsubsection}[subsection]
\newcounter{paragraph}[subsubsection]

\def\thesection{\arabic{section}}
\def\thesubsection{\arabic{section}.\arabic{subsection}}
\def\thesubsubsection{\arabic{section}.\arabic{subsection}.\arabic{subsubsection}}

%% Section headings are numbered 1. Xxx, 2. Yyy, etc. in 14 pt. bold “Small Caps” Times New Roman font with a 6 pt. line spacing following.

% \@startsection {NAME}{LEVEL}{INDENT}{BEFORESKIP}{AFTERSKIP}{STYLE} 
%            optional * [ALTHEADING]{HEADING}
\def\section{
	\@startsection{section}{1}{0pt}%
	{-12pt minus -\parskip}
	{.1\parskip minus \parskip} % .1 larger than 6 pt, closest we can get
	{\Large\bfseries\scshape\refstepcounter{section}\thesection. }%
}%

%% Subsection headings are numbered 1.1. Aaa, 1.2. Bbb, etc. in 12 pt. bold Times New Roman font with a 6pt line spacing following.
\def\subsection{
	\@startsection{subsection}{2}{0pt}%
	{-12pt minus -\parskip}
	{.1\parskip minus \parskip} % .1 larger than 6 pt, closest we can get
	{\large\bfseries\refstepcounter{subsection}\thesubsection. }%
}%

\def\subsubsection{
	\@startsection{subsection}{2}{0pt}%
	{-12pt minus -\parskip}
	{.1\parskip minus \parskip} % .1 larger than 6 pt, closest we can get
	{\large\bfseries\refstepcounter{subsubsection}\thesubsubsection. }%
}%

% ============================================================================
% 2.7 Text
% ============================================================================
% Main-body text is to written in fully (left and right) justified 11 pt. Times New Roman font with a 6pt. (paragraph) line spacing following the last line of each paragraph, but a 12pt. (paragraph) line spacing following the last paragraph.

% this is already standard, paragraph spacing set above 

% ============================================================================
% Appendix
% ============================================================================
\def\appendix{
	\@startsection{appendix}{2}{0pt}%
	{-12pt minus -\parskip}
	{.1\parskip minus \parskip}
	{\large\scshape A. }%
}%

% ============================================================================
% Caption
% ============================================================================
\newlength\abovecaptionskip
\newlength\belowcaptionskip
\setlength\abovecaptionskip{10\p@}
\setlength\belowcaptionskip{0\p@}

\long\def\@makecaption#1#2{%
	\vskip\abovecaptionskip
	\sbox\@tempboxa{#1: #2}%
	\ifdim \wd\@tempboxa >\hsize
		#1: #2\par
	\else
		\global \@minipagefalse
		\hb@xt@\hsize{\hfil\box\@tempboxa\hfil}%
	\fi
	\vskip\belowcaptionskip%
}

% ============================================================================
% Figures
% ============================================================================
\newcounter{figure}
\renewcommand\thefigure{\@arabic\c@figure}
\def\fnum@figure{\figurename\nobreakspace\thefigure}
\def\fps@figure{tbp}
\def\ftype@figure{1}
\def\ext@figure{lof}

\newcommand\figurename{Figure}

\newenvironment{figure}
	{\@float{figure}}
	{\end@float}
\newenvironment{figure*}
	{\@dblfloat{figure}}
	{\end@dblfloat}

% ============================================================================
% Footnotes
% ============================================================================
% Do not use any footnotes!
\def\footnotesize{\small}
% print a rule above footnotes 
\renewcommand\footnoterule{%
	\kern-3\p@
	\hrule\@width.5\columnwidth
	\kern2.6\p@}

% creates the footnote text, indented with mark
\newcommand\@makefntext[1]{%
	\parindent 1em%
	\noindent
	\hb@xt@1.8em{\hss\@makefnmark}#1}

% replaces the footnote and prints a warning instead
% comment out to be able to use footnotes
\renewcommand\footnote[1]{
	\@latex@warning{AIRCC: Do not use any footnotes!}
}
% ============================================================================
% List / Itemize / Enumeration settings
% ============================================================================
% itemized list label styles
\def\labelitemi{$\scriptstyle\bullet$}
\def\labelitemii{\textbf{--}}
\def\labelitemiii{$\ast$}
\def\labelitemiv{$\cdot$}

% enum labels
\newcommand\labelenumi{\theenumi.}
\newcommand\labelenumii{(\theenumii)}
\newcommand\labelenumiii{\theenumiii.}
\newcommand\labelenumiv{\theenumiv.}

% level indents
\setlength\leftmargini  {2.5em}
\setlength\leftmarginii  {2.2em}
\setlength\leftmarginiii {1.87em}
\setlength\leftmarginiv  {1.7em}

\leftmargin  \leftmargini

% labels general
\setlength\labelsep{.5em}
\setlength\labelwidth{\leftmargini}
\addtolength\labelwidth{-\labelsep}

%\renewenvironment{enumerate}%
%{%
%	\setcounter{enumi}{0}% restart numbering
%	\smallskip% skip a line
%	\renewcommand{\item}{% new definition of item
%		\par% start a new line
%		\parindent 1.5em
%		\hangindent=1.5em
%		\refstepcounter{enumi}% advance counter
%		\makebox[2.0em][l]{(\theenumi)}% print counter to width of 3em, aligned to left
%	}% end of definition of item
%}{% at end of environment
%	\par% start new paragraph
%	\smallskip% skip a line
%}

% ============================================================================
% Bibliography
% ============================================================================
\newcommand\refname{References}
\newcommand\newblock{\hskip .11em\@plus.33em\@minus.07em}

\newdimen\bibindent
\setlength\bibindent{1.0em}

\newenvironment{thebibliography}[1]
	{%
		\section*{\refname}%
		\list{\@biblabel{\@arabic\c@enumiv}}%
		{\settowidth\labelwidth{\@biblabel{#1}}%
			\leftmargin\labelwidth
			\advance\leftmargin\labelsep
			\usecounter{enumiv}%
		}%
		\sloppy
		\clubpenalty4000
		\@clubpenalty \clubpenalty
		\widowpenalty4000%
	}
	{\def\@noitemerr
		{\@latex@warning{Empty `thebibliography' environment}}%
	\endlist}

\let\oldbibliography\thebibliography
\renewcommand{\thebibliography}[1]{% add 6pt spacing between bibitems
  \oldbibliography{#1}%
  \setlength{\itemsep}{5pt}%
}

% ============================================================================
% Tables
% ============================================================================
\newcommand\tablename{Table}

% array and table seperations
\setlength\arraycolsep{5\p@}
\setlength\arrayrulewidth{.4\p@}
\setlength\tabcolsep{6\p@}
\setlength\doublerulesep{2\p@}
\setlength\tabbingsep{\labelsep}

% table counters
\newcounter{table}
\renewcommand\thetable{\@arabic\c@table}
\def\fps@table{tbp}
\def\ftype@table{2}
\def\ext@table{lot}
\def\fnum@table{\tablename\nobreakspace\thetable}

% table environment
\newenvironment{table}
	{\@float{table}}
	{\end@float}
\newenvironment{table*}
	{\@dblfloat{table}}
	{\end@dblfloat}

\endinput
