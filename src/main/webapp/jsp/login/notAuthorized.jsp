<%@page import="it.eng.spagoCore.configuration.ConfigSingleton"%>
<%@ include file="../../include.jsp"%>

<sl:html>
    <sl:head title="Accesso Negato" />
    <sl:body>
        <sl:header description="Accesso Negato" showHomeBtn="false"/>
        <div class="newLine "></div>

        <div id="menu">&nbsp;</div>
        <div id="content">
            <!-- Message Box -->
            <div class="messages  plainError ">
                <ul>
                    <li class="message  error  ">Utente non autorizzato alla visualizzazione della risorsa ${requestScope.destination} richiesta. <a href="./Logout.html" title="Fai logout ">Effettua un logout</a> </li>
                </ul>
            </div>
        </div>
        
        <!--Footer-->
        <sl:footer />
    </sl:body>
</sl:html>
