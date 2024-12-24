package servlets;

import java.sql.*;
import java.io.*;
import jakarta.servlet.*;

import constants.IOnlineBookStoreConstants;
import sql.IBookConstants;

public class ReceiptServlet extends GenericServlet {
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        res.setContentType(IOnlineBookStoreConstants.CONTENT_TYPE_TEXT_HTML);
        try {
            Connection con = DBConnection.getCon();
            PreparedStatement ps = con.prepareStatement("select * from " + IBookConstants.TABLE_BOOK);
            ResultSet rs = ps.executeQuery();
            int i = 0;

            // Include ViewBooks.html
            RequestDispatcher rd = req.getRequestDispatcher("ViewBooks.html");
            rd.include(req, res);

            // Header for successful payment message
            pw.println("<div class=\"tab\">You Successfully Paid for the Following Books</div>");

            // Start of table
            pw.println(
                "<div class=\"tab\">" +
                "<table>" +
                "   <tr>" +
                "       <th>Book Code</th>" +
                "       <th>Book Name</th>" +
                "       <th>Book Author</th>" +
                "       <th>Book Price</th>" +
                "       <th>Quantity</th>" +
                "       <th>Amount</th>" +
                "   </tr>"
            );

            double total = 0.0;

            while (rs.next()) {
                int bPrice = rs.getInt(IBookConstants.COLUMN_PRICE);
                String bCode = rs.getString(IBookConstants.COLUMN_BARCODE);
                String bName = rs.getString(IBookConstants.COLUMN_NAME);
                String bAuthor = rs.getString(IBookConstants.COLUMN_AUTHOR);
                int bQty = rs.getInt(IBookConstants.COLUMN_QUANTITY);
                i++;

                String qt = "qty" + i;
                int quantity = Integer.parseInt(req.getParameter(qt));

                try {
                    String check1 = "checked" + i;
                    String getChecked = req.getParameter(check1);

                    if (bQty < quantity) {
                        pw.println(
                            "</table><div class=\"tab\">Please Select a Quantity Less Than the Available Books</div>"
                        );
                        break;
                    }

                    if ("pay".equals(getChecked)) {
                        int amount = bPrice * quantity;
                        total += amount;

                        // Add book details to table
                        pw.println(
                            "<tr>" +
                            "   <td>" + bCode + "</td>" +
                            "   <td>" + bName + "</td>" +
                            "   <td>" + bAuthor + "</td>" +
                            "   <td>" + bPrice + "</td>" +
                            "   <td>" + quantity + "</td>" +
                            "   <td>" + amount + "</td>" +
                            "</tr>"
                        );

                        // Update book quantity in database
                        bQty -= quantity;
                        PreparedStatement ps1 = con.prepareStatement(
                            "update " + IBookConstants.TABLE_BOOK +
                            " set " + IBookConstants.COLUMN_QUANTITY + "=? where " +
                            IBookConstants.COLUMN_BARCODE + "=?"
                        );
                        ps1.setInt(1, bQty);
                        ps1.setString(2, bCode);
                        ps1.executeUpdate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Add total row to table
            pw.println(
                "   <tr>" +
                "       <td colspan='5' style='text-align:right; font-weight:bold;'>Total Paid Amount:</td>" +
                "       <td style='font-weight:bold;'>" + total + "</td>" +
                "   </tr>"
            );

            // Close table
            pw.println("</table></div>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
