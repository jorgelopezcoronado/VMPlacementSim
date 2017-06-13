Maximize
	5 e11 + 5 e12 + 4 e13 + 2 e14 + 5 e21 + 5 e22 + 4 e23 + 2 e24 + 5 e31 + 5 e32 + 4 e33 + 2 e34

Subject To
CPUH1:	3 e11 + 3 e12 + 2 e13 + 1 e14 <= 3 
RAMH1:  2 e11 + 3 e12 + 2 e13 + 1 e14 <= 2
CPUH2:	3 e21 + 3 e22 + 2 e23 + 1 e24 <= 3 
RAMH2:  2 e21 + 3 e22 + 2 e23 + 1 e24 <= 3
CPUH3:	3 e31 + 3 e32 + 2 e33 + 1 e34 <= 3 
RAMH3:  2 e31 + 3 e32 + 2 e33 + 1 e34 <= 2
pc'14:  e14 >= 1
pc'23:  e23 >= 1
pc'31:	e31 >= 1

Integers
	e11 e12 e13 e14 e21 e22 e23 e24 e31 e32 e33 e34

End

