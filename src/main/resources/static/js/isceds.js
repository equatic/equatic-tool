$(function () {
    $('.expand').click(function () {
        var broadIsced = $(this).closest('li.broad-isced');
        var expanded = broadIsced.data('expanded') == "yes";
        var narrowIsceds = broadIsced.find('ul.narrow-isceds');

        if (expanded) {
            narrowIsceds.hide();
            broadIsced.data('expanded', "no");
            broadIsced.attr('data-expanded', "no");
        } else {
            narrowIsceds.show();
            broadIsced.data('expanded', "yes");
            broadIsced.attr('data-expanded', "yes");
        }
    });

    $('input.checkbox-broad-isced').change(function () {
        var narrowIsceds = $(this).closest('li.broad-isced').find('input.checkbox-narrow-isced');

        narrowIsceds.prop('checked', this.checked).change();
    });

    $('input#checkbox-select-all-isceds').click(function () {
        $('#select-isceds').find('input.checkbox-broad-isced').prop('checked', this.checked).change();
    });

    $('input.checkbox-isced').change(function () {
        var allCheckboxesSelected = $('input.checkbox-isced:not(:checked)').length == 0;

        $('input#checkbox-select-all-isceds').prop('checked', allCheckboxesSelected).change();
    });
});