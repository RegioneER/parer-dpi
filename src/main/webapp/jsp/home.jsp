<%@ include file="../include.jsp"%>


<sl:html>
    <sl:head title="Home page" >
        <script type='text/javascript' >
    
            $(document).on('click', 'table.listaNews > tbody > tr.news > td > img.apriChiudi', apriChiudiNewsDaFreccia);
            
            function CTableHandler() {
                this.load = CTableHandlerLoad;
            }
    
            function CTableHandlerLoad() {                
                var righeNascoste = $("table > tbody > tr.nascondiRiga");
                CMostraRighe(righeNascoste);
            }
    
            function CMostraRighe(righeNascoste) {
                righeNascoste.hide();
        
            }
    
            function apriChiudiNewsDaFreccia(event) {
                if ($(this).parent().parent().is('.rigaVisibile')) {
                    $(this).parent().parent().next().hide();
                    $(this).parent().parent().removeClass('rigaVisibile');
                    //$(this).attr('src', './img/toolBar/closeGreen.png');
                    $(this).attr('src', './img/window/aperto.png');
                } else{
                    $(this).parent().parent().next().show();
                    $(this).parent().parent().addClass('rigaVisibile');
                    //$(this).attr('src', './img/toolBar/openGreen.png');
                    $(this).attr('src', './img/window/chiuso.png');
                }
            }
            $(document).ready(function() {
                var imm = $("img.apriChiudi", this);
                //imm.attr('src', './img/toolBar/closeGreen.png');
                imm.attr('src', './img/window/aperto.png');
                var tableHandler = new CTableHandler();
                tableHandler.load();
        
                $("table.listaNews > tbody > tr.news:first img.apriChiudi").trigger('click');
            });
        </script>
            
    </sl:head>

    <sl:body>
        <sl:header showChangeOrganizationBtn="false" />
        <sl:menu showChangePasswordBtn="true" />
        <sl:content>

            <slf:fieldSet borderHidden="true" >
               
                <p>Benvenuto!</p>
                <table class="listaNews" style="table-layout: fixed; width: 100%" >
                    <c:forEach items="${news}" var="current">
                        <tr class="news" >
                       
                            <td style="width:15px;cursor:pointer"><img alt="Leggi News"  class="apriChiudi" src="./img/window/chiuso.png" /></td>
                            <td><p style="text-align:left; word-wrap: break-word;"><c:out value="${current.dsOggetto }" escapeXml="false" /></p></td>
                                   <%-- <td><c:out value="${current.dtIniPubblic}" /><td> --%>
                        </tr>
                        <tr class="nascondiRiga">
                            <td>&nbsp;</td>
                            <%-- <td><p style="text-align:left"><c:out value="${current.dlTesto}" /></p><td> --%>
                            <td style="word-wrap:break-word; "><c:out value="${current.dlTesto}" escapeXml="false"/></td>
                               
                        </tr>
                    </c:forEach>
                </table>
            </slf:fieldSet>

        </sl:content>
        <sl:footer />
    </sl:body>

</sl:html>
