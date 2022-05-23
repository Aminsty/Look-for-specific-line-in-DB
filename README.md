# Look-for-specific-word-in-Database
Program to look for a specific word in all of the database using threads to minimize search time, the use of threads makes the program search through each column in every table in the database at the same time, for example with 4 tables each containing 3 columns, we'll have 12 threads with each thread looking through a column at the same time, hence a minimized search time.
The program requests the user first to give him the word he wants to search for, then requests for the name of the database where he wants to search, and it either returns the table and column in which the word was found in this format (line-COLUMN) or an empty string meaning the word does not exist in this database.
