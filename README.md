Mu Prototype Compiler - archived
===

This project was a compiler prototype for the Mu micro virtual machine. The project was carried out in parallel of
designing the Mu micro virtual machine so that it allows us to quickly understand our design, especially
the performance implication. The project also serves as a learning project for me to understand optimizing 
back-end compiler.  

The Zebu VM largely inherits the knowledge we learnt from this project. But this project, as a prototype, is more
aggressive in terms of picking techniques for the compiler. However, one major difference between Zebu compiler 
and this prototype is the instruction selector. This prototype implements a 
BURS-style [code generator-generator](backend/codegengen). 
We describe hardware architecture along with code generation rules with a [description file](backend/x86/x64.target),
and the code generator-generator will derive the actual backend for emitting the code with weight-based
bottom-up rewriting. Though this approach is common in large production compilers, it is an overkill
for Mu's simple instruction sets with considerable complexity increase. In the end, in the implementation
of Zebu, we abandoned this approach, and used a simpler tree pattern matching.  