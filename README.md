Maven-version-bumper
====================

* Rationale

  The need for this tool comes from a desire to have precise control when bumping versions in large multi-module
  projects where the modules individual version cycles aren't always synchronized.

** The current problem

  There are a number of different Maven plugins (for example releases and versions) that does a fair job
  tackling this problem, but they have a number of shortcomings:

    * They employ a rather sweeping attitude that everything should be updated everywhere always.

    * They use the Maven project model to navigate and locate possible dependencies to update that leaves you few
      options to back out if you need more precise control of what gets updated when.

    * Most update behaviour in these plugins are not fully documented and you have to search the source code to
      find out what is actually going on under rather sweeping generalizations in the documentation.

  This project is an experiment in an other approach of tackling the "Dependency Version Hell" problem.

** Dependency Version Hell

  Any fairly large scale project that uses Maven soon finds the need to organize its code in a number of different
  modules. This need comes from the fact different parts of the project needs to be released at different times but
  still have a common code base whose versions needs to be tightly controlled.

  In the early stages of development all the modules gets their versions bumped at the same time and all the
  module dependencies can be bumped as well. Life is easy.

  After a while modules starts to become more finished, and different teams within the project have different needs
  of these modules. Some need the old stable one and some needs the bleeding edge. I dependency terms this means that
  you can't bump everything at the same time any longer. And the matrix gets more complicated as time goes on. Errors
  here causes frustration and lost development time while fixing problems not in the code, but in the dependencies.
  Life is now a bit complicated.

  All this boils down to the stage when the initial development project is done and the mature code moves into the
  a maintenance phase with a very strong need for tight control of what gets included in each release.

  If you have kept a good mindfulness of how you administrate your dependencies from early on, life can be quite
  easy, but if you think that it will probably fix itself over time chances are good that you can now welcome
  "Dependency Version Hell" into your life.

** A suggestion to an alternate solution

  The existing plugins release and versions have the same goal here, to automate the repetitive work of keeping the
  dependencies under control. It is not the goal that the author objects to, but rather the method of getting there.

  Maven Version Bumper is an experiment to try out a different approach to solve the same problem. The author
  doesn't claim that this is the best solution, but argues that you can't know this until you have tried.

  Maven Version Bumper:

    * lets you build short and concise update scenarios with minimal side effects to give you
      a precise control of what gets updated when and to what.

    * makes use of BeanShell for writing scenario files.

    * lets you dry run your scenario to find missing updates.

    * integrates with your Version Control System to integrate commit messages and tagging/labeling.

    * can use the Version Control System to prepare for test builds and then revert the changes after the test.

    * can be used to find any lingering SNAPSHOT dependencies that could possibly indicate forgotten modules.


* Scope of the tool

  This tool is a building block in the larger release process in that it tries to create a clear, dense and simple
  syntax, a DSL if you like, for the task of defining what versions that should get updated.

  It also has a simple integration towards version control so that you can automate the committing of pom.xml-files
  and tagging of the modules at the same time.

  Besides this the tools tries to makes as few dependencies of its own on its environment. If you need integration with
  an Issue Management System, you have to do it outside this tool. Connection to you build server? Same thing.

  Do a few things, and do them well.
