# CSV Data Handler

**A console utility program that allows getting a selection of the cheapest N products from the input CSV files, but no more than M products with the same ID.
  Used parallel processing to increase performance.
  Reading and handling data from file in parts to save memory.**

**Initial Data:**
1. Several CSV files. The number of files can be quite large (up to 100,000).
2. The number of rows within each file can reach up to several million.
3. Each file contains 5 columns: 
    - Product ID (int),
    - Name (String), 
    - Condition (String), 
    - State (String), 
    - Price (double).
4. The same product IDs may occur more than once in different CSV files and in the same CSV file.


## How to use the program

#### 1. Run main.Main

##### Pass the following arguments : 
__directoryPath__ <br> 
__delimiter (defaultValue: ,)__ <br> 
__productResultRowsCount (defaultValue: 1000)__ <br> 
__duplicateProductsMaxCount (defaultValue: 20)__ <br> <br>

Example:
```arguments
directoryPath=C:\Users\Tiran\Desktop\files\csv delimiter=, productResultRowsCount=1000 duplicateProductsMaxCount=20  
```

#### 2. After the process, you must specify the path to the output file.