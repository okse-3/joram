include ../Makefile.global

clean:
	rm -f *.exe *.o *~
	rm -f test1 test2 test3 test4 test5 test6 test7 test8 testAdmin

tests: test1 test2 test3 test4 test5 test6 test7 test8 testAdmin

test1:	TestXoram1.o
	$(LD) -o test1 TestXoram1.o -L$(LIBDIR) -lxoram

test2:	TestXoram2.o
	$(LD) -o test2 TestXoram2.o -L$(LIBDIR) -lxoram

test3:	TestXoram3.o
	$(LD) -o test3 TestXoram3.o -L$(LIBDIR) -lxoram

test4:	TestXoram4.o
	$(LD) -o test4 TestXoram4.o -L$(LIBDIR) -lxoram

test5:	TestXoram5.o
	$(LD) -o test5 TestXoram5.o -L$(LIBDIR) -lxoram

test6:	TestXoram6.o
	$(LD) -o test6 TestXoram6.o -L$(LIBDIR) -lxoram

test7:	TestXoram7.o
	$(LD) -o test7 TestXoram7.o -L$(LIBDIR) -lxoram

test8:	TestXoram8.o
	$(LD) -o test8 TestXoram8.o -L$(LIBDIR) -lxoram

testAdmin:	TestXoramAdmin.o
	$(LD) -o testAdmin TestXoramAdmin.o -L$(LIBDIR) -lxoram

.C.o:
		$(CC) -o $*.o -c $*.C
