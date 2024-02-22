# Report for assignment 3


Group 18 - [repo](https://github.com/Deadlica/jabref)


## Project
Jabref is a reference and citation management tool built in java.

[JabRef Github](https://github.com/JabRef/jabref)

## Onboarding experience
We started with an android app project that can convert images to PDF (and pdfs to images) but the documentation was very limited and there was no guide on how to build the program or run the tests. At first we tried to build and run the project with Gradle since the project included Gradle build files. Building the project this way worked, but running the tests did not. Then we realized that the project was an android studio project but even when using Android Studio we did not manage to get the tests to run. We therefore decided to change to another project.


The second project that we decided on was JabRef since it provided extensive documentation on how to get started. JabRef provided extensive documentation on how to setup up a fork of the repository locally to get started with contributing. The guide also included instructions on how to set up the project using IntelliJ which led us to also download IntelliJ for the setup. They included how to build the project, run tests and adding a code style checker to ensure that contributions follow their coding style. For the build they also documented which JDK was needed for it to run and any extra components would be installed by gradle when building. The build and tests ran on the first attempt without any issues.




## Complexity


We chose to count the complexity of these 5 functions:
   * AuthorListParser::getAuthor
   * PicaXmlParser::parseEntry
   * GroupDialogViewModel::setupValidation
   * PdfContentImporter::getEntryFromPDFContent
   * IsiImporter::importDatabase


### What are your results for five complex functions?


Function                 | CC (Lizard) | CC (Manual counting)
------------------------ | --------    |     ----------
getAuthor()              | 45          | 42
parseEntry()             | 82          | 76
compare()                | 26          | 6
getEntryFromPDFContent() | 53          | 52
importDatabase()         | 53          | 57
 
There is a slight difference in the cyclomatic complexity, where most of our counts were less than what Lizard counted. We believe that is likely due that Lizard doesn't take return points into consideration, which explains why we typically counted less.


### Are the functions just complex, or also long?


In most cases the long functions tend to have large cyclomatic complexity but there were still a few cases with either short functions that had large complexity or long functions that also had very little complexity in relation to its size.


### What is the purpose of the functions?


* getAuthor() parses author names of references and reformats it.
* The purpose of parseEntry() was to convert an HTML element to a BibEntry.
* The purpose of setupValidation() was to set up validation rules for properties of external file types.
* getEntryFromPDFContent() generates a BibEntry by parsing information in a String from a PDF.
* The purpose of importDatabase() was to parse input from a BufferedReader into a list of BibEntry objects.


### Are exceptions taken into account in the given measurements?


When counting manually we counted try catch the same way we counted if-else statements and we counted throws as exit points. 






### Is the documentation clear w.r.t. all the possible outcomes?
Setupvalidation() had no documentation at all and parseEntry() was only documented in german. getAuthor() was fairly well documented. getEntryFromPDFContent() was also documented fairly well with an overall description and comments throughout the method. The documentation is however not according to the JavaDoc standard.






## Refactoring


Plan for refactoring complex code:


### processArguments()
JabRef has built-in support to run some tasks from the command line. The purpose of processArguments() is to go through and check what arguments were passed along the program in the CLI and act accordingly to perform the tasks required. Lizard reports that the cyclomatic complexity of processArguments() is 36. The issue is that the process of processing what arguments were given can’t be done in a methodology that doesn’t check what arguments were passed and for each argument act accordingly to what needs to be done for that task. This is most likely why the method itself is simply a long list of sequential if statements that look similar to “if flag A, do action A” and so on.


If I were to refactor the method regardless I would probably “mask” all the if statements that correspond to a flag by putting them each in a new method. These methods could be called something like “handleFlagA()” and so on… Thereafter I could simply call each method after each other sequentially or have some sort of Map that has the flag names as the key and the function itself as the value. With a Map like this you could simply iterate through the arguments given and apply them as the keys in the map to execute the necessary functions.

I wouldn’t suggest this refactoring as it simply just hides the code which in turn reduces the readability of the code for future improvements of this method.


### getDescription(Field)
First of all, the complexity of getDescription according to lizard is 103 but in reality it’s much lower. Most of the function is just a switch statement with a lot of cases (which adds to complexity) but what lizard doesn’t take into account is that they all return a value. This means that while Lizard states that the cyclomatic complexity is 103, it is in fact not much higher than 10. With that said, there are some ways that could be used to reduce the complexity. 

Currently, the function checks if the Field object is of type standardField, specialField or internalField and then has a switch for each type. One way to reduce the complexity would be to declare the function in the Field interface and then override it with the switch for the classes StandardField, specialField and internalField. 

Another option would be to replace each switch with a Hashmap. All (key,value) pairs would have to be added to the hashmap but by doing so the entire switch could be replaced with a constant lookup. If the hashmap is set up once and stored between calls of the function it could also increase performance since it would essentially replace a linear search with a constant one. 


### getEntryFromPDFContent()
The high complexity of getEntryFromPDFContent() comes from the fact that each PDF has lots of information which has to be extracted to create a BibEntry. Also, the format differs slightly between PDFs, causing additional complexity. For example, there is a special case where the first line of the PDF input contains "Conference", in which case it should be handled differently. It would be possible to place parts of the code from the method into smaller helper functions to reduce the complexity, but I don't think this would be good practice. This is because the method does one concise task, which is to extract all the BibEntry fields from a PDF.


### getLayoutFormatterByName(String name)
This function gets manually counted complexity to 72 and 74 according to Lizard. The high complexity in this function comes from a large switch case with many cases that cause a significant amount of branching. In this case, high complexity does not make the function difficult to understand but can make it harder to read. Lowering the complexity of this function is fairly simple because of the large switch case. Any data structure that holds the values for each name type as a key could reduce almost all complexity. There are not any drawbacks to doing this but it won't improve readability directly. The advantage of this refactoring could be a minor improvement in performance since the switch case needs to linearly go though all the cases but a hash map for example could do this in constant time. However, since the its only around 80 cases, this won't increase in performance will be extremely small.   


## Coverage


### Tools


The tools we used to check coverage were Codecov and Jacoco.


For Codecov we were able to simply utilize the original repository's Codecov workflow which would redirect us to a website that would display coverage information in an interface that was easy to grasp. The main downside that we found with Codecov was that it didn't seem to report code coverage per method but only on the entire class.


Using Jacoco was also simple to setup as it was already configured with the project after following their setup guide. A simple button click in IntelliJ would run all tests and generate HTML pages with coverage reports. Jacoco could also report code coverage per method instead of per class which made it easy for us to compare it with our manual coverage tools.


### Your own coverage tool


| Function (Including link to Manual Coverage commit)                                                                                                     | JaCoCo | Manual Coverage |
|--------------------------------------------------------------------------------------------------------------|--------|-----------------|
| [ArgumentProcessor::processArguments()](https://github.com/Deadlica/jabref/commit/0e087f1698e05d77324bfe4f14a31920d96fd88b)    | 44%    | 24.24%          |
| [LayoutEntry::getLayoutFormatterByName()](https://github.com/Deadlica/jabref/commit/e024db90ab005bfc2ff65c7e0852c597ce36085f)  | 37%    | 37.5%              |
| [PdfContentImporter::getEntryFromPDFContent()](https://github.com/Deadlica/jabref/commit/8a121bd04e352df027b1448cc9781cba5cc0c807) | 49%    | 34.6%           |
| [FieldNameLabel::getDescription()](https://github.com/Deadlica/jabref/commit/b1c0b8ae5eb1613ebc22ff5c1285bb6067681994)        | 0%     | 0%              |
| [EntryComparator::compare()](https://github.com/Deadlica/jabref/commit/dittCommitNummer)              | 75%    | 75%             |


### Evaluation


1. How detailed is your coverage measurement?

The coverage measurement is very detailed as it tests whether every single branch in the function is reached. However, the tool only tracks whether a branch has been reached or not reached. Additional details such as what path was taken and what conditions were met are currently not given and could be an improvement for the tool.   


2. What are the limitations of your own tool?

As the current tool is hardcoded for specific functions, using the same approach to other functions would be a lot of work. Our tool also requires some manual work to calculate the coverage. For example, it currently only keeps track of which branches have been reached for each time the function is called. This means that if the function is for example called 100 times in the tests, you would have to compile 100 outputs into 1 to assert the branch coverage. 


3. Are the results of your tool consistent with existing coverage tools?

As can be seen in the table above, the branch coverage of JaCoco is roughly the same as our model for some functions while it is vastly different in others.  



## Coverage improvement


| Function                                                 | JaCoco | Manual Coverage | Author | Tests Added      |
|--------------------------------------------------------|--------|-----------------|-------------|-------------|
| [ArgumentProcessor::processArguments()](https://github.com/Deadlica/jabref/commit/commit_hash) |    TBA    |      TBA           |       Samuel Greenberg      |    4         |
| [LayoutEntry::getLayoutFormatterByName()](https://github.com/Deadlica/jabref/commit/7bab53816b491197b938973fea0f79e01234623c)  |    51%    |       51.3%          |      Noel Tesfalidet       |       7      |
| [PdfContentImporter::getEntryFromPDFContent()](https://github.com/Deadlica/jabref/commit/461e57619e08fbb3aec7a166b800e24c47aa83fb) |   63%     |        44%         |      André Fredriksen       |      2       |
| [FieldNameLabel::getDescription()](https://github.com/Deadlica/jabref/commit/3d825e841f34a945c6cdfe4a84c5be511d7881cd)          |   6%   |       5.2%      |    Gustaf Larsson      |  3   |
| [EntryComparator::compare()](https://github.com/Deadlica/jabref/commit/commit_hash)                |        |                 |             |             |




## Self-assessment: Way of working


After the first assignment, the group assessed that the reached state was "In place". This was mainly because the group had implemented several ways of working and had started to complete real work with the practices. The only point that needed to be added was that the group needed more time to reflect upon the methods of working. We have now had more time to reflect and improve upon our ways of working. Therefore we feel that our current state is closer to "Working well" than previously. However, there are still several ways of working that can be improved. We have continuously changed the practices used based on the task at hand and what the assignments require and we feel more comfortable with the tools. Some aspects of the tool usage come more naturally now compared to previously but some things still require thinking and asking other group members about what the correct usage is. It is a bit unclear when exactly the group fulfills this point but the group still feels that we are not completely there yet.

The group's biggest potential for improvement is still probably better planning and dividing tasks faster. It is difficult to find a time when every group member has time to sit down and go through the assignment and create a plan. To mitigate this, two potential solutions have been proposed, one is that all group members understand the entire assignment and make preparations before so the meeting time can be shorter. The second one is to start the planning of the assignment directly after the examination where the team is already in one place.          




## Overall experience
The main takeaway is that open source requires a large time investment and efforts to be able to make significant contributions to the project. Many functions rely on other functions and truly understanding a function is therefore often very time consuming. We also realised that JabRef is already fairly well tested, especially functions that are simple to test. 

We also realized how important good documentation is and how great of a difference it can make for new developers joining the project. The first open source project that we looked at was extremly difficult to get into and we barely understood how to make the necessary installations. The second project was significantly easier to get into and install and the guide on how to get started was very detailed.  
