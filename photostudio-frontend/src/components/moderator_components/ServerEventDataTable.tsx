"use client";
import * as React from "react";
import {
  type ColumnDef,
  type SortingState,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
} from "@tanstack/react-table";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  ChevronFirst,
  ChevronLast,
  ChevronLeft,
  ChevronRight,
  X,
} from "lucide-react";
import {getFilteredEvents} from "@/api/ApiEventController";
import type { EventItem } from "@/pages/ModeratorPage/moderatorColumns";
import type { EventStatus, EventType } from "@/lib/constants";

interface ServerEventDataTableProps {
  columns: ColumnDef<EventItem, any>[];
  /** Increment this to trigger a data refresh from the parent */
  refreshKey?: number;
}

export function ServerEventDataTable({
  columns,
  refreshKey = 0,
}: ServerEventDataTableProps) {
  // ── Server filter state ──
  const [search, setSearch] = React.useState("");
  const [locationSearch, setLocationSearch] = React.useState("");
  const [status, setStatus] = React.useState<EventStatus | undefined>(
    undefined,
  );
  const [type, setType] = React.useState<EventType | undefined>(undefined);
  const [dateFrom, setDateFrom] = React.useState<string | undefined>(
    new Date().toJSON().slice(0, 10),
  );
  const [dateTo, setDateTo] = React.useState<string | undefined>(undefined);

  // ── Pagination state ──
  const [page, setPage] = React.useState(0);
  const [pageSize, setPageSize] = React.useState(10);

  // ── Data state ──
  const [data, setData] = React.useState<EventItem[]>([]);
  const [totalRows, setTotalRows] = React.useState(0);
  const [totalPages, setTotalPages] = React.useState(0);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  // ── Sorting (client-side on current page) ──
  const [sorting, setSorting] = React.useState<SortingState>([]);

  // ── Debounced search ──
  const [debouncedSearch, setDebouncedSearch] = React.useState("");
  const [debouncedLocationSearch, setDebouncedLocationSearch] =
    React.useState("");
  React.useEffect(() => {
    const timeout = setTimeout(() => {
      setDebouncedSearch(search);
      setDebouncedLocationSearch(locationSearch);
      setPage(0); // reset to first page on search change
    }, 500);
    return () => clearTimeout(timeout);
  }, [search, locationSearch]);

  // Reset to first page when filters change
  React.useEffect(() => {
    setPage(0);
  }, [status, type, dateFrom, dateTo, pageSize]);

  // ── Fetch data ──
  React.useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await getFilteredEvents({
          page,
          pageSize,
          search: debouncedSearch || undefined,
          location: debouncedLocationSearch || undefined,
          status,
          type,
          onlyWithFreeSpots: false,
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
        });
        if (!cancelled) {
          setData(result.content ?? []);
          setTotalRows(result.totalElements ?? 0);
          setTotalPages(result.totalPages ?? 0);
        }
      } catch (err) {
        console.error("Failed to fetch events:", err);
        if (!cancelled) {
          setError("Nie udało się załadować wydarzeń");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [
    page,
    pageSize,
    debouncedSearch,
    debouncedLocationSearch,
    status,
    type,
    dateFrom,
    dateTo,
    refreshKey,
  ]);

  const table = useReactTable({
    data,
    columns,
    manualPagination: true,
    pageCount: totalPages,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onSortingChange: setSorting,
    state: {
      sorting,
      pagination: { pageIndex: page, pageSize },
    },
  });

  const hasActiveFilters =
    debouncedSearch || debouncedLocationSearch || status || type || dateFrom || dateTo;

  function clearFilters() {
    setSearch("");
    setDebouncedSearch("");
    setLocationSearch("");
    setDebouncedLocationSearch("");
    setStatus(undefined);
    setType(undefined);
    setDateFrom(undefined);
    setDateTo(undefined);
    setPage(0);
  }

  if (error) {
    return <p className="p-4 text-destructive">{error}</p>;
  }

  return (
    <>
      {/* ── Filters ── */}
      <div className="flex flex-wrap items-end gap-3 py-4">
        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Szukaj</span>
          <Input
            className="h-8 w-48"
            placeholder="Szukaj po nazwie…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Lokalizacja</span>
          <Input
            className="h-8 w-48"
            placeholder="Szukaj po lokalizacji…"
            value={locationSearch}
            onChange={(e) => setLocationSearch(e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Status</span>
          <Select
            value={status ?? "__all__"}
            onValueChange={(v) =>
              setStatus(v === "__all__" ? undefined : (v as EventStatus))
            }
          >
            <SelectTrigger className="h-8 w-[130px]">
              <SelectValue placeholder="Wszystkie" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__all__">Wszystkie</SelectItem>
              <SelectItem value="PLANNED">Zaplanowane</SelectItem>
              <SelectItem value="COMPLETED">Zakończone</SelectItem>
              <SelectItem value="CANCELLED">Anulowane</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Typ</span>
          <Select
            value={type ?? "__all__"}
            onValueChange={(v) =>
              setType(v === "__all__" ? undefined : (v as EventType))
            }
          >
            <SelectTrigger className="h-8 w-[130px]">
              <SelectValue placeholder="Wszystkie" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__all__">Wszystkie</SelectItem>
              {[
                "KWF",
                "SKN",
                "WRSS",
                "URSS",
                "AGH",
                "AKRE",
                "CM",
                "AKT",
                "PRIVATE",
              ].map((t) => (
                <SelectItem key={t} value={t}>
                  {t}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Data od</span>
          <Input
            type="date"
            className="h-8 w-40"
            value={dateFrom ?? ""}
            onChange={(e) => setDateFrom(e.target.value || undefined)}
          />
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-xs text-muted-foreground">Data do</span>
          <Input
            type="date"
            className="h-8 w-40"
            value={dateTo ?? ""}
            onChange={(e) => setDateTo(e.target.value || undefined)}
          />
        </div>

        {hasActiveFilters && (
          <Button
            variant="ghost"
            onClick={clearFilters}
            className="h-8 px-2 lg:px-3"
          >
            Wyczyść filtry
            <X className="ml-2 h-4 w-4" />
          </Button>
        )}
      </div>

      {/* ── Table ── */}
      <div className="rounded-md border h-full">
        <Table className="overflow-y-scroll h-full">
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => {
                  const canSort = header.column.getCanSort();
                  const sortState = header.column.getIsSorted();

                  return (
                    <TableHead key={header.id}>
                      {header.isPlaceholder ? null : (
                        <div
                          className={
                            canSort
                              ? "cursor-pointer select-none inline-flex items-center gap-1"
                              : ""
                          }
                          onClick={header.column.getToggleSortingHandler()}
                        >
                          {flexRender(
                            header.column.columnDef.header,
                            header.getContext(),
                          )}
                          {canSort &&
                            (sortState === "asc" ? (
                              <ArrowUp className="h-4 w-4 text-muted-foreground" />
                            ) : sortState === "desc" ? (
                              <ArrowDown className="h-4 w-4 text-muted-foreground" />
                            ) : (
                              <ArrowUpDown className="h-4 w-4 text-muted-foreground" />
                            ))}
                        </div>
                      )}
                    </TableHead>
                  );
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="h-24 text-center"
                >
                  <span className="text-muted-foreground">Ładowanie…</span>
                </TableCell>
              </TableRow>
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && "selected"}
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id}>
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext(),
                      )}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="h-24 text-center"
                >
                  Brak wyników.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* ── Pagination ── */}
      <div className="flex items-center justify-between space-x-2 py-4">
        <div className="hidden md:block text-sm text-muted-foreground">
          {totalRows} Liczba wierszy
        </div>
        <div className="flex items-center gap-2 place-content-between w-full">
          <div className="flex items-center gap-1">
            <span className="text-sm">Na stronę</span>
            <Select
              value={`${table.getState().pagination.pageSize}`}
              onValueChange={(value) => {
                const nextPageSize = Number(value);
                setPageSize(nextPageSize);
                table.setPageSize(nextPageSize);
              }}
            >
              <SelectTrigger className="h-8 w-[70px]">
                <SelectValue placeholder={pageSize} />
              </SelectTrigger>
              <SelectContent side="top">
                {[10, 20, 30, 40, 50].map((size) => (
                  <SelectItem key={size} value={`${size}`}>
                    {size}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex items-center gap-1 ">
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                onClick={() => setPage(0)}
                disabled={page === 0}
              >
                <ChevronFirst className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Input
                type="number"
                min={1}
                max={totalPages || 1}
                defaultValue={page + 1}
                key={page} // re-render input when page changes
                onChange={(e) => {
                  const p = e.target.value ? Number(e.target.value) - 1 : 0;
                  if (p >= 0 && p < totalPages) {
                    setPage(p);
                  }
                }}
                className="h-8 w-16"
              />
            </div>
            /{totalPages || 1}
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage(totalPages - 1)}
              disabled={page >= totalPages - 1}
            >
              <ChevronLast className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </>
  );
}
