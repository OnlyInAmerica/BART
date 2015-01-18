package pro.dbro.bart;

import pro.dbro.bart.api.xml.BartApiResponse;

/**
 * Created by davidbrodsky on 1/18/15.
 */
public interface ResponseRefreshListener {
    public void refreshRequested(BartApiResponse oldResponse);
}