var TableDatatablesAjax = function () {

    var initTable = function () {
        var table = $('#feedHistoryTable');
        var dt = table.DataTable({
            "language": {
                "aria": {
                    "sortAscending": ": activate to sort column ascending",
                    "sortDescending": ": activate to sort column descending"
                },
                "emptyTable": "No data available in table",
                "info": "Showing _START_ to _END_ of _TOTAL_ entries",
                "infoEmpty": "No entries found",
                "infoFiltered": "(filtered1 from _MAX_ total entries)",
                "lengthMenu": "_MENU_ entries",
                "search": "Search:",
                "zeroRecords": "No matching open houses found"
            },
            "columnDefs": [
            	{ "width": "15%", "targets": [0,1] },
            ],
            "lengthMenu": [
                [50, 100, 150, 200, -1],
                [50, 100, 150, 200, "All"] // change per page values here
            ],

            // set the initial value
            "pageLength": 50,
        });

       /* table.find('tfoot th:not(:last-child):not(:first-child)').each( function () {
            var title = $(this).text();
            $(this).html( '<input type="text" class="form-control form-filter input-sm" placeholder="Search '+title+'" />' );
        } );*/
        
        table.find('.group-checkable').change(function () {
            var set = jQuery(this).attr("data-set");
            var checked = jQuery(this).is(":checked");
            jQuery(set).each(function () {
                if (checked) {
                    $(this).prop("checked", true);
                    $(this).parents('tr').addClass("active");
                } else {
                    $(this).prop("checked", false);
                    $(this).parents('tr').removeClass("active");
                }
            });
        });

        table.on('change', 'tbody tr .checkboxes', function () {
            $(this).parents('tr').toggleClass("active");
        });

/*        dt.columns().every( function () {
            var that = this;

            $( 'input', this.footer() ).on( 'keyup change', function () {
                if ( that.search() !== this.value ) {
                    that
                        .search( this.value )
                        .draw();
                }
            } );
        } );*/
    }

    return {
        init: function () {
            initTable();
        }

    };

}();

jQuery(document).ready(function() {
    TableDatatablesAjax.init();
});