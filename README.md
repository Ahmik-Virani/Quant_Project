# Alpha Engine – Limit Orders

This repository is a modified and enhanced version of [AntonVonGolub/Code](https://github.com/AntonVonGolub/Code), tailored for research and experimentation in high-frequency trading using limit order data.

The only active and maintained component in this repository is the `LimitOrders` directory, which contains the core logic for the Alpha Engine.

## 🚀 Getting Started

### Step 1: Download Data

Download historical tick-level FX data from [TrueFX Historical Data](https://www.truefx.com/truefx-historical-downloads/).

- Save the `.csv` files inside a directory named `Data` located in the root of the project.

### Step 2: Configure Input File

Open the Java file `AlphaEnginePublic.java` inside the `LimitOrders/` directory.

Modify the filename at **line 15** to point to the desired CSV file you downloaded.

### Step 3: Compile and Run
Return to the root directory of the project and execute the provided shell script.
```
./shell.sh
```

This script will:

 - Compile all .java files in the LimitOrders directory.
 - Execute AlphaEnginePublic.java

### Directory Structure
```
Alpha-Engine/
├── Data/                  # Contains input .csv data (ignored in .gitignore)
├── LimitOrders/           # Contains Java source files for Alpha Engine
│   ├── AlphaEngine.java
│   ├── AlphaEnginePublic.java
│   ├── CoastlineTrader.java
│   ├── LimitOrder.java
│   ├── LocalLiquidity.java
│   ├── Price.java
│   ├── ReadData.java
│   ├── Runner.java
│   └── Price.java
├── shell.sh               # Script to compile and run the engine
├── README.md              # You're here!
```


### Credits:

**The Alpha Engine: Designing an Automated Trading Algorithm**  
Golub, Anton and Glattfelder, James B. and Olsen, Richard B.  
High Performance Computing in Finance  
Chapman & Hall/CRC Series in Mathematical Finance  
2017  

A preprint is available at [SSRN](https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2951348).

#### Abstract

*We introduce a new approach to algorithmic investment management that yields profitable automated trading strategies. 
This trading model design is the result of a path of investigation that was chosen nearly three decades ago. Back then, 
a paradigm change was proposed for the way time is defined in financial markets, based on intrinsic events. 
This definition lead to the uncovering of a large set of scaling laws. An additional guiding principle was 
found by embedding the trading model construction in an agent-base framework, inspired by the study of complex 
systems. This new approach to designing automated trading algorithms is a parsimonious method for building a new 
type of investment strategy that not only generates profits, but also provides liquidity to financial markets and 
does not have a priori restrictions on the amount of assets that are managed.*