package ThymeLeaf;

public class Thyme {

    public static final String DIGEST_TEMPLATE = """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org" lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <meta name="x-apple-disable-message-reformatting">
              <title>GMS Digest</title>
              <style>
                body { margin: 0; padding: 0; width: 100% !important; -webkit-text-size-adjust: 100%; }
                a { text-decoration: none; }
                @media screen and (max-width: 600px) {
                  .gms-outer { padding: 16px 12px !important; }
                  .gms-card { padding: 20px 16px !important; }
                  .gms-btn { display: block !important; margin: 0 0 8px !important; text-align: center !important; }
                }
              </style>
            </head>
            <body style="margin:0;padding:0;background-color:#F4F5F7;"
                  th:with="acct=${account != null ? account : (#lists.isEmpty(responses) ? null : responses[0].account)},
                           mailBase=${acct != null ? 'https://mail.google.com/mail/?authuser=' + #uris.escapeQueryParam(acct) + '#all/' : 'https://mail.google.com/mail/#all/'},
                           catTotal=${responses == null ? 0 : #lists.size(responses)}">

              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="width:100%;background-color:#F4F5F7;">
                <tr>
                  <td align="center" class="gms-outer" style="padding:32px 16px;">

                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0"
                           style="width:100%;max-width:600px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;color:#1F2328;">

                      <!-- Header -->
                      <tr>
                        <td style="padding:0 4px 20px;">
                          <h1 style="margin:0;font-size:20px;line-height:1.3;font-weight:600;color:#1F2328;">GMS Digest</h1>
                          <p th:if="${catTotal > 0}" style="margin:4px 0 0;font-size:14px;line-height:1.5;color:#656D76;"
                             th:text="${catTotal == 1 ? '1 category' : catTotal + ' categories'}">3 categories</p>
                        </td>
                      </tr>

                      <!-- Empty state -->
                      <tr th:if="${catTotal == 0}">
                        <td class="gms-card" style="padding:28px 24px;background-color:#E6E9ED;border:1px solid #D0D7DE;border-radius:8px;text-align:center;">
                          <p style="margin:0;font-size:16px;font-weight:600;">You're all caught up</p>
                          <p style="margin:6px 0 0;font-size:14px;color:#656D76;">There are no new emails to summarize right now.</p>
                        </td>
                      </tr>

                      <!-- One card per category -->
                      <th:block th:each="r : ${responses}"
                                th:with="cat=${r.category == null ? '' : #strings.toUpperCase(#strings.trim(r.category))},
                                         accent=${cat == 'IMPORTANT' ? '#CF222E' : (cat == 'NORMAL' ? '#0969DA' : (cat == 'PROMOTIONAL_SPAM' ? '#8250DF' : '#656D76'))},
                                         label=${cat == '' ? 'Uncategorized' : (cat == 'IMPORTANT' ? 'Important' : (cat == 'NORMAL' ? 'Normal' : (cat == 'PROMOTIONAL_SPAM' ? 'Promotions & spam' : #strings.replace(cat, '_', ' '))))},
                                         senderCount=${r.senders == null ? 0 : #arrays.length(r.senders)},
                                         msgCount=${r.ids != null ? #arrays.length(r.ids) : senderCount},
                                         idsQuery=${actionBaseUrl != null and r.ids != null and #arrays.length(r.ids) > 0 ? '?ids=' + #uris.escapeQueryParam(#strings.arrayJoin(r.ids, ',')) : null}">
                        <tr>
                          <td class="gms-card" style="padding:24px;background-color:#E6E9ED;border:1px solid #D0D7DE;border-radius:8px;"
                              th:styleappend="${'border-left:4px solid ' + accent + ';'}">

                            <!-- Category + count -->
                            <p style="margin:0;font-size:16px;line-height:1.4;font-weight:600;">
                              <span th:style="${'color:' + accent + ';'}" th:text="${label}">Important</span>
                              <span style="font-size:13px;font-weight:400;color:#656D76;"
                                    th:text="${' · ' + (msgCount == 1 ? '1 email' : msgCount + ' emails')}"> · 2 emails</span>
                            </p>

                            <!-- Senders: each links to its message in Gmail -->
                            <p th:if="${senderCount > 0}" style="margin:8px 0 0;font-size:13px;line-height:1.6;color:#656D76;">
                              <span>From: </span><th:block th:each="s, st : ${r.senders}"
                                  th:with="hasAddr=${s != null and #strings.contains(s, '<') and #strings.contains(s, '>')},
                                           rawName=${hasAddr ? #strings.trim(#strings.substringBefore(s, '<')) : (s == null ? '' : #strings.trim(s))},
                                           cleanName=${#strings.trim(#strings.replace(rawName, '&quot;', ''))},
                                           name=${#strings.isEmpty(cleanName) or #strings.contains(cleanName, '@') ? 'Unknown sender' : cleanName},
                                           mid=${r.ids != null and st.index lt #arrays.length(r.ids) ? r.ids[st.index] : null}"><a
                                  th:href="${mid != null ? mailBase + #uris.escapePathSegment(mid) : null}"
                                  style="color:#0969DA;text-decoration:none;" th:text="${name}">Sender</a><span th:unless="${st.last}">, </span></th:block>
                            </p>

                            <!-- AI summary: pre-line keeps the blank lines between "Mail N:" sections -->
                            <p th:if="${!#strings.isEmpty(#strings.trim(r.summary))}"
                               style="margin:16px 0 0;font-size:15px;line-height:1.6;color:#1F2328;white-space:pre-line;word-wrap:break-word;overflow-wrap:break-word;"
                               th:text="${#strings.trim(r.summary)}">Mail 1: Summary goes here.</p>
                            <p th:if="${#strings.isEmpty(#strings.trim(r.summary))}"
                               style="margin:16px 0 0;font-size:14px;color:#656D76;font-style:italic;">No summary was generated for this category.</p>

                            <!-- Actions: map to GmailActionController (/mark-read, /mark-important, /delete) -->
                            <div th:if="${idsQuery != null}" style="margin:20px 0 0;padding:16px 0 0;border-top:1px solid #E1E4E8;">
                              <a class="gms-btn" th:href="${actionBaseUrl + '/mark-read' + idsQuery}"
                                 style="display:inline-block;margin:0 8px 8px 0;padding:8px 14px;border:1px solid #D0D7DE;border-radius:6px;background-color:#F6F8FA;color:#1F2328;font-size:13px;font-weight:500;text-decoration:none;">Mark all as read</a><a
                                 th:if="${cat != 'IMPORTANT'}" class="gms-btn" th:href="${actionBaseUrl + '/mark-important' + idsQuery}"
                                 style="display:inline-block;margin:0 8px 8px 0;padding:8px 14px;border:1px solid #D0D7DE;border-radius:6px;background-color:#F6F8FA;color:#1F2328;font-size:13px;font-weight:500;text-decoration:none;">Mark as important</a><a
                                 class="gms-btn" th:href="${actionBaseUrl + '/delete' + idsQuery}"
                                 style="display:inline-block;margin:0 8px 8px 0;padding:8px 14px;border:1px solid #D0D7DE;border-radius:6px;background-color:#FFFFFF;color:#CF222E;font-size:13px;font-weight:500;text-decoration:none;">Move all to Trash</a>
                            </div>
                          </td>
                        </tr>
                        <tr><td style="height:16px;line-height:16px;font-size:0;">&nbsp;</td></tr>
                      </th:block>

                      <!-- Footer -->
                      <tr>
                        <td align="center" style="padding:8px 16px 0;text-align:center;">
                          <p style="margin:0;font-size:12px;line-height:1.6;color:#8C959F;">Generated by GMS &mdash; Gmail Management Service. Summaries are AI-generated; open the original messages to confirm important details.</p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;
}