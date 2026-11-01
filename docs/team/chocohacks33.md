---
layout: page
title: Thirumoorthi Prakash Kumar's Project Portfolio
---

## OSPS

OSPS is a desktop application for debt recovery agents. An agent can use keyboard commands to find a debtor,
check the outstanding balance and review earlier interactions before making a call. My main contribution was
the worklist that helps the agent decide which record to open next.

## Finding and organising debtors

I added the `list` command and the outstanding balance on each debtor card. An agent can see how much a debtor
owes without opening each profile. My tests cover an empty list and check that the displayed records stay
up to date after a change. [List command](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/48)
and [balance display tests](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/55).

A caller might give only part of a name. I extended `find` to match that text anywhere in the name, ignoring case,
while keeping phone searches exact. I also added sorting within the current results and filtering by an inclusive
range of outstanding balances. For example, an agent can find debtors with a shared surname and then sort just
those matches by amount. Two debtors with the same amount remain distinguishable by their persistent IDs.
The view can change order without rewriting the order of the saved records.
[Partial-name search](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/160),
[sorting](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/161) and
[amount filtering](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/159).

## Testing, documentation and review

* **Testing the packaged application.** I added a CI test that starts the packaged JAR, saves a record and
  starts a second Java process to check that the data can be loaded again. I extended it to exercise damaged
  save files. That matters because a successful first launch tells us little about what an agent will find on
  their second day of using OSPS. [Restart tests](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/179)
  and [recovery tests](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/183).
* **Guides.** I defined the target user and value proposition in the Developer Guide and updated the sequence
  diagram that traces a search through the application. In the User Guide I clarified the difference between a
  debtor's persistent ID and their position on screen, which can change after a search or sort.
  [Product scope](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/45),
  [search diagram](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/160) and
  [guide changes](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/210).
* **Peer review.** During review of follow-up scheduling, I raised the case where setting the same date twice
  could add a misleading history entry. I also asked that rescheduling preserve the previous date so an agent
  can tell what changed. [Repeated-date review](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/154#discussion_r4414058649)
  and [history review](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/154#discussion_r4414058668).

My contributions can be explored in the [course code dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=chocohacks33&breakdown=true).
I used AI assistance in development, testing, reviews and drafting this portfolio.
