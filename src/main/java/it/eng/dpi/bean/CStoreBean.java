/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.dpi.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CStoreBean {

    private static final Logger log = LoggerFactory.getLogger(CStoreBean.class);
    private int numImagesInStudy;
    private int numSeriesInStudy;
    private int transferedSeriesInStudy;
    private int transferedImagesInStudy;

    // DAV
    private int numFramesInStudy;
    private int numImagesNoFrame;

    public int getNumImagesInStudy() {
        return numImagesInStudy;
    }

    public void setNumImagesInStudy(int numImagesInStudy) {
        this.numImagesInStudy = numImagesInStudy;
    }

    public int getNumSeriesInStudy() {
        return numSeriesInStudy;
    }

    public void setNumSeriesInStudy(int numSeriesInStudy) {
        this.numSeriesInStudy = numSeriesInStudy;
    }

    public int getTransferedSeriesInStudy() {
        return transferedSeriesInStudy;
    }

    public int getTransferedImagesInStudy() {
        return transferedImagesInStudy;
    }

    public void incTransferedImagesInStudy() {
        transferedImagesInStudy++;
    }

    public void incTransferedSeriesInStudy() {
        transferedSeriesInStudy++;
    }

    // DAV
    public int getNumFramesInStudy() {
        return numFramesInStudy;
    }

    public void incNumFramesInStudy(int numOfFrames) {
        numFramesInStudy = numFramesInStudy + numOfFrames;
    }

    public int getNumImageNoFrame() {
        return numImagesNoFrame;
    }

    public void incNumImageNoFrame() {
        numImagesNoFrame++;
    }

    // DAV
    public boolean isStudyComplete(boolean acceptMore) {
        if (acceptMore) {
            return isStudyCompleteAcceptMore();
        } else {
            return isStudyComplete();
        }
    }

    // DAV
    // aggiunto perché le modality NM di tipo Diacam (Modena) non considerano i multiframe quindi li sommo durante il
    // controllo
    public boolean isStudyCompleteWithMultiframe(boolean acceptMore) {
        if (acceptMore) {
            return isStudyCompleteWithMultiframeAcceptMore();
        } else {
            return isStudyCompleteWithMultiframe();
        }
    }

    // vecchio metodo Marco
    public boolean isStudyComplete() {
        return (numImagesInStudy == transferedImagesInStudy) && (numSeriesInStudy == transferedSeriesInStudy);
    }

    public boolean isStudyCompleteAcceptMore() {
        // aggiunto <= perché nel caso di PACS Fuji i report strutturati non vengono contati come immagini da trasferire
        return (numImagesInStudy <= transferedImagesInStudy) && (numSeriesInStudy == transferedSeriesInStudy);
    }

    public boolean isStudyCompleteWithMultiframe() {
        log.debug("isStudyCompleteWithMultiframe - Image no frame: " + numImagesNoFrame + "; - numImagesInStudy: "
                + numImagesInStudy + "; - numFramesInStudy: " + numFramesInStudy + "; - transferedImagesInStudy: "
                + transferedImagesInStudy);
        int numFrameAndNoFrame = numImagesNoFrame + numFramesInStudy;
        log.debug("somma immagini con frame e senza frame  = " + numFrameAndNoFrame);
        return (isStudyComplete()
                || ((numImagesInStudy == numFrameAndNoFrame) && (numSeriesInStudy == transferedSeriesInStudy)));
    }

    public boolean isStudyCompleteWithMultiframeAcceptMore() {
        log.debug("isStudyCompleteWithMultiframeAcceptMore - Image no frame: " + numImagesNoFrame
                + "; - numImagesInStudy: " + numImagesInStudy + "; - numFramesInStudy: " + numFramesInStudy
                + "; - transferedImagesInStudy: " + transferedImagesInStudy);
        int numFrameAndNoFrame = numImagesNoFrame + numFramesInStudy;
        log.debug("somma immagini con frame e senza frame  = " + numFrameAndNoFrame);
        // TODO: Verificare questo controllo
        if (numFramesInStudy > 0) {
            return (isStudyCompleteAcceptMore()
                    || ((numImagesInStudy == numFrameAndNoFrame) && (numSeriesInStudy == transferedSeriesInStudy)));
        } else {
            return isStudyCompleteAcceptMore();
        }
        // return (isStudyCompleteAcceptMore() || ((numImagesInStudy <= numFrameAndNoFrame) && (numSeriesInStudy ==
        // transferedSeriesInStudy)));
    }

    // MEV#34069
    public boolean isStudyCompleteAcceptMoreSerie() {
        // aggiunto >= perché nel caso di PACS AGFA collegati al VNA le serie in rejected (IOCM KOS Reject) non vengono
        // contate come serie da
        // trasferire
        return (numImagesInStudy == transferedImagesInStudy) && (numSeriesInStudy >= transferedSeriesInStudy);
    }
    // end MEV#34069

}
