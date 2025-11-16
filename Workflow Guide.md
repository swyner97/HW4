# Workflow Guide

1. Clone the Repository (first time only)

git clone <repo-url>
cd <repo-folder>

2. Create Your Feature Branch

git checkout dev
git pull origin dev   # Get the latest dev changes
git checkout -b feature/your-name-task

3. Work on Your Feature

git add .
git commit -m "Add: feature description"


4. Keep Your Branch Updated

git checkout dev
git pull origin dev         # Get latest dev updates
git checkout feature/your-name-task
git merge dev               # Merge dev into your branch
# Resolve conflicts if any, then:
git add .
git commit -m "Merge dev into feature/your-name-task"


5. Push Your Feature Branch

git push origin feature/your-name-task


6. Create a Pull Request (PR) → dev

Open a Pull Request from your feature branch → dev.
Another teammate must review & approve before merging.

7. Merging into main

Only after testing and team approval, we merge dev → main.
This keeps main clean and production-ready.



Common Commands: 

Pull latest changes from a branch:

git checkout <branch>
git pull origin <branch>

Merge another branch into your branch:

git checkout your-branch
git merge other-branch

Check current branch:

git branch

Undo last local commit (without losing changes):

git reset --soft HEAD~1

