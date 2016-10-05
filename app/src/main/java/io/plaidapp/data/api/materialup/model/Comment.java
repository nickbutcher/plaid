
package io.plaidapp.data.api.materialup.model;

import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.plaidapp.util.DribbbleUtils;

public class Comment {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("html_body")
    @Expose
    private String htmlBody;
    @SerializedName("user")
    @Expose
    private Maker user;
    @SerializedName("created_at")
    @Expose
    private String createdAt;

    public Spanned parsedBody;

    /**
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The body
     */
    public String getBody() {
        return body;
    }

    /**
     * 
     * @param body
     *     The body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 
     * @return
     *     The htmlBody
     */
    public String getHtmlBody() {
        return htmlBody;
    }

    /**
     * 
     * @param htmlBody
     *     The html_body
     */
    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    /**
     * 
     * @return
     *     The user
     */
    public Maker getUser() {
        return user;
    }

    /**
     * 
     * @param user
     *     The user
     */
    public void setUser(Maker user) {
        this.user = user;
    }

    /**
     * 
     * @return
     *     The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public Spanned getParsedBody(TextView textView) {
        if (parsedBody == null && !TextUtils.isEmpty(body)) {
            parsedBody = DribbbleUtils.parseDribbbleHtml(body, textView.getLinkTextColors(),
                    textView.getHighlightColor());
        }
        return parsedBody;
    }
}
