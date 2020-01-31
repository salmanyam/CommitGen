CommitGen
=============
The project aims to generate commit messages from source code differences between two versions of a program. The source code differences are described using a natural language and then the natural language description is translated into commit messages using a pre-trained neural machine translation model.

This project is a part of CS 5704 (Software Engineering) course. This project uses GumTree to identify changes between the head of a Git repository and the current status of a repository and describes the changes in natural languages (e.g. English). The natural language is further improved by using a neural machine translation technique.

GumTree
=======
This project utilizes the fine-grained source code change tool called GumTree. We modify the implementation to add additional functionalities to support the automatic commit generation. The modified gumtree implementation is availabe at https://github.com/salmanyam/gumtree-modified.

How to use
============
### Buidling GumTree
    1. Download the modified gumtree source code from https://github.com/salmanyam/gumtree-modified.
    2. Follow the following commands to build the GumTree-modified sources.
```
git clone https://github.com/salmanyam/gumtree-modified.git
cd gumtree-modified
./gradlew build -x test
```
    3. Browse gumtree-modified/dist/build/distributions and unzip gumtree.zip.

### Building CommitGen
    1. Add the required jar files from the unzipped folder in the previous 
    step (step 3 in building gumtree) to the libs folder in the CommitGen project.
    2. Then issue the following commands to build and run the CommitGen program.
```
git clone https://github.com/salmanyam/CommitGen.git
cd CommitGen
./gradlew build
./gradlew run -PappArgs="['repo path', 'output filename']"
```
    3. Here, the repo path is the complete path to a git repository and output 
    filename is file where the generated output will be stored. For example, repo 
    path = /Users/<username>/eclipse-workspace/hadoop/.git and filename = output.txt

### Preprocessing
All the preprocessing scripts are inside the scripts folder and the datasets are inside the datasets folder.

### Training and Testing Neural Machine Translation
We use the neural machine translation models from the following link https://github.com/tensorflow/nmt. Please go through the GitHub page of the translation package to get more information

Please Note
===========
There is still lot to do for the project. This project is completed for the CS5704 project. But, we are still working on the project. Stay tuned for the upgraded verison!

