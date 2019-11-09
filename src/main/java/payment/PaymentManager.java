package payment;

import ui.Ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import common.AlphaNUSException;
import project.ProjectManager;

//@@author karansarat
/**
 * PaymentManager for managing Payments objects and PaymentForms from the
 * PaymentsList.
 */
public abstract class PaymentManager {

    private static Field strToField(String str) {
        switch (str) {
        case ("PAYEE"):
            return Field.PAYEE;
        case ("EMAIL"):
            return Field.EMAIL;
        case ("MATRIC"):
            return Field.MATRIC;
        case ("PHONE"):
            return Field.PHONE;
        case ("ITEM"):
            return Field.ITEM;
        case ("COST"):
            return Field.COST;
        case ("INVOICE"):
            return Field.INV;
        case ("STATUS"):
            return Field.STATUS;
        case ("DEADLINE"):
            return Field.DEADLINE;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Finds the Payments objects containing a payee name and returns a list of
     * Payments.
     *
     * @param payee Payee of the item.
     */
    public static Payee findPayee(ProjectManager projectManager, String name) {
        Set<String> projectnames = projectManager.projectmap.keySet();
        if (projectManager.currentProject == null) {
            projectManager.gotoProject(projectnames.iterator().next());
        }
        String currProject = projectManager.currentProject.projectname;
        while (!projectManager.currentProject.managermap.containsKey(name)) {
            projectnames.remove(currProject);
            if (projectnames.isEmpty()) {
                throw new IllegalArgumentException();
            }
            currProject = projectnames.iterator().next();
            projectManager.gotoProject(currProject);
        }
        return projectManager.currentProject.managermap.get(name);
    }

    /**
     * Edits the Payments object details, may overload string to take different ways
     * of inputs.
     */
    public static void editPayee(String payee, String inv, String fieldToAmend, String replace,
            HashMap<String, Payee> managermap, Ui ui) {
        Field field = strToField(fieldToAmend);
        if (inv.isEmpty()) {
            if (field == Field.PAYEE) {
                managermap.get(payee).payee = replace;
            } else if (field == Field.EMAIL) {
                managermap.get(payee).email = replace;
            } else if (field == Field.MATRIC) {
                managermap.get(payee).matricNum = replace;
            } else if (field == Field.PHONE) {
                managermap.get(payee).phoneNum = replace;
            }
            ui.printEditMessage(managermap.get(payee));
        } else {
            for (Payments payment : managermap.get(payee).payments) {
                if (payment.inv.equals(inv)) {
                    if (field == Field.ITEM) {
                        payment.item = replace;
                    } else if (field == Field.COST) {
                        payment.cost = Double.parseDouble(replace);
                    } else if (field == Field.INV) {
                        payment.inv = replace;
                    } else if (field == Field.STATUS) {
                        if (replace.equalsIgnoreCase("pending")) {
                            payment.status = Status.PENDING;
                        } else if (replace.equalsIgnoreCase("approved")) {
                            payment.status = Status.APPROVED;
                        } else if (replace.equalsIgnoreCase("overdue")) {
                            payment.status = Status.OVERDUE;
                        }
                    }
                    ui.printEditMessage(payment, payee);
                    break;
                }
            }
            assert (false); // Invalid invoice number <-- TODO : Raise error
        }
    }

    /**
     * List the Payments object details, may extend to generate statement of
     * accounts.
     */
    public static ArrayList<ArrayList<Payments>> listOfPayments(HashMap<String, Payee> managermap) {
        ArrayList<ArrayList<Payments>> listOfPayments = new ArrayList<>();
        ArrayList<Payments> overdue = new ArrayList<>();
        ArrayList<Payments> pending = new ArrayList<>();
        ArrayList<Payments> approved = new ArrayList<>();
        for (Payee payee : managermap.values()) {
            for (Payments payment : payee.payments) {
                if (payment.status == Status.PENDING) {
                    pending.add(payment);
                } else if (payment.status == Status.OVERDUE) {
                    overdue.add(payment);
                } else {
                    approved.add(payment);
                }
            }
        }
        listOfPayments.add(pending);
        listOfPayments.add(overdue);
        listOfPayments.add(approved);
        return listOfPayments;
    }


    /**
     * Deletes the Payments object details.
     * 
     */
    public static Payments deletePayments(String payee, String item, HashMap<String, Payee> managermap) {
        int i = 0;
        while (i < managermap.get(payee).payments.size()) {
            if (managermap.get(payee).payments.get(i++).item.equals(item)) {
                Payments deleted = new Payments(payee, item, managermap.get(payee).payments.get(--i).cost,
                        managermap.get(payee).payments.get(i).inv);
                managermap.get(payee).payments.remove(i);
                return deleted;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Add the Payments object details to PaymentsList.
     * 
     * @throws AlphaNUSException
     */
    public static Payments addPayments(String payee, String item, double cost, String inv,
            HashMap<String, Payee> managermap, Set<String> dict) throws AlphaNUSException {
        Payments pay = new Payments(payee, item, cost, inv);
        pay.paymentToDict(dict);
        managermap.get(payee).payments.add(pay);
        return pay;
    }

    /**
     * Add Payee object to managermap.
     */
    public static Payee addPayee(String project, String payee, String email, String matricNum, String phoneNum,
                                 HashMap<String, Payee> managermap) {
        if (managermap.keySet().contains(payee)) {
            throw new IllegalArgumentException();
        }
        Payee payeeNew = new Payee(project, payee, email, matricNum, phoneNum);
        managermap.put(payee, payeeNew);
        return payeeNew;
    }

    /**
     * Delete Payee object.
     */
    public static Payee deletePayee(String payee, HashMap<String, Payee> managermap) {
        Payee payeeDeleted = managermap.get(payee);
        managermap.remove(payee);
        return payeeDeleted;
    }
}
