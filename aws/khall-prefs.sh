#!/bin/bash
export PAGER=less
export EDITOR=vi
export VISUAL=vi
export TMPDIR=$HOME/tmp
export TABSIZE=8
export TZ=CST6CDT
export LC_COLLATE=C
export LC_CTYPE=C
export LC_ALL=C
alias ls='ls -aCF'
alias more='less'
alias rm='rm -f'

if [ "$TERM" = "xterm" -o "$TERM" = "screen" ]; then
        if [ "$(id -u)" -eq 0 ]; then
	        PS1="\[\033[33m\]\h \w #\[\033[0m\] \[\033]0;\u@\h:\w\007\]"
        else
        	PS1="\[\033[33m\]\h \w \$\[\033[0m\] \[\033]0;\u@\h:\w\007\]"
	fi
else
	PS1="\[\033[33m\]\h \w \$\[\033[0m\] "
fi

