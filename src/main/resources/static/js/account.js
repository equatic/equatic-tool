$(function () {
    /**
     * Enables elements that require an institution to be selected.
     */
    $('#select-inst').on('isSelectedInstitutionChanged', function (event, isSelectedInstitution) {
        var requiresSelectedInst = $(this).closest('form').find('.requires-selected-inst');

        if (isSelectedInstitution) {
            requiresSelectedInst.removeAttr('disabled').show();
        } else {
            requiresSelectedInst.attr('disabled', 'disabled');
        }
    });

    /**
     * Shows elements that require that selected institution doesn't use federated IdP.
     */
    $('#select-inst').on('isInstitutionWithFederatedIdPChanged', function (event, isInstitutionWithFederatedIdp) {
        var requiresInstWoFederatedIdP = $(this).closest('form').find('.requires-inst-wo-federated-idp');

        if (isInstitutionWithFederatedIdp) {
            requiresInstWoFederatedIdP.addClass('hide');
        } else {
            requiresInstWoFederatedIdP.removeClass('hide');
        }
    });

    $('#button-register').click(function () {
        var form = $(this).closest('form');
        form.attr('action', $(this).attr('action')).attr('method', 'get').submit();
    });

    $('#link-forgot-password').click(function () {
        var form = $(this).closest('form');
        form.attr('action', $(this).attr('action')).attr('method', 'get').submit();
    });
});

