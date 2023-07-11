# Music
- Software Engineering Course Project, Spring 2023
- Team Members: `Abhijith Anil`, `Jatin Agarwala`, `Pratyay Suvarnapathaki`, `Sneha Raghava Raju`, `Yash Mehan`

## Project Description
Course project, with the aim of to applying the concepts learned in class to improve an existing software system, [Music by Sismics](https://github.com/sismics/music).

---

## Part One: Reverse Engineering and Refactoring
**Documentation at `/docs/part1.pdf` and `/docs/part1_extras.pdf`**

1. Analysed the existing codebase using UML diagrams, OOP Principles and SWOT Analysis for the classes comprising the following four subsystems:
  - User management 
  - Library management
  - Last.fm integration
  - Administrator features

2. Identified software design smells, and the corresponding code smells for each subsystem. Tools used: SonarQube, Designite.

3. Proposed and idenitified code metrics to quantify the impact and technical debt incurred by the code smells. Tools used: PMD, CheckStyle.

4. Proposed and implemented improvements to the codebase, to remove the code smells and improve the code quality. Analysed impact via updated metrics. Tools used: IntelliJ IDEA.

Extras: Analysed the stateful behavior of the system using the Transition Systems Model proposed by Prof. Venkatesh Choppella et. al. in their [AlgoDynamics](https://algodynamics.gitlab.io/) approach. Also, proposed a hypothetical automated refactoring pipeline using search-based and ML techniques.

---

## Part Two: Feature Additions and Design Patterns
**Documentation at `/docs/part2.pdf` and `/docs/part2_extra.pdf`**

1. Better User Management: Improved the UX and DevEx of the login system, allowing the creation of new users directly from the login page, with the scope for alternate login methods in the future. Design pattern used: Chain of Responsibility.

2. Better Library Management: Added the ability to mark playlists and individual tracks as public or private, and to share public playlists with other users. Design pattern used: DAO.

3. Online Integration: Added the ability to search for tracks on Soptify and LastFm, and to add them to the library. Also integrated Spotify and LastFm recommendation functionality based on existing playlist contents. Design pattern used: Design pattern used: Strategy.

Extra: Added collaborative playlists, allowing multiple users to add tracks to a playlist.