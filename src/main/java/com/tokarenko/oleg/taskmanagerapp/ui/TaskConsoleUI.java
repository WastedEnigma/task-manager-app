package com.tokarenko.oleg.taskmanagerapp.ui;

import com.tokarenko.oleg.taskmanagerapp.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Task Console User Interface class
*/

public class TaskConsoleUI extends ConsoleUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskConsoleUI.class);

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        String option;

        do {
            System.out.println("(1) Add new task");
            System.out.println("(2) Edit task");
            System.out.println("(3) Remove task");
            System.out.println("(4) Share task with another user");
            System.out.println("(5) Return to main page");

            System.out.print("Choose option: ");
            option = sc.nextLine();

            switch (option) {
                case "1":
                    createAndAddTask();
                    break;
                case "2":
                    editTask();
                    break;
                case "3":
                    removeTask();
                    break;
                case "4":
                    shareTask();
                    break;
                default:
                    if (!option.equalsIgnoreCase("5")) {
                        LOGGER.error("Invalid input");
                    }
            }
        } while (!option.equalsIgnoreCase("5"));
    }

    private void createAndAddTask() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter content: ");
        String content = sc.nextLine();

        TaskService.createAndAddTask(content);
    }

    private void editTask() {
        Scanner sc = new Scanner(System.in);
        long id = getValidId();

        System.out.print("Enter new content: ");
        String content = sc.nextLine();

        TaskService.editTask(id, content);
    }

    private void removeTask() {
        TaskService.removeTask(getValidId());
    }

    private void shareTask() {
        Scanner sc = new Scanner(System.in);
        long id = getValidId();

        System.out.print("Share with (enter username): ");
        String username = sc.nextLine();

        TaskService.shareTask(id, username);
    }

    private long getValidId() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter task id: ");

        if (sc.hasNextLong()) {
            return sc.nextLong();
        }

        LOGGER.error("Invalid id type, try again");

        return getValidId();
    }
}
