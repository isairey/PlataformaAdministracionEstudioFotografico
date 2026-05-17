"use client"

export default function Home() {
  return (
    <div>
      <div className="flex flex-col p-8 min-h-screen">
        <div className="max-w-4xl mx-auto w-full flex flex-col">
          <h1 className="text-3xl font-semibold mb-8">Witaj ponownie</h1>
          
          <div className="space-y-8">
            
            <div>
              <h2 className="text-xl font-semibold mb-4">Szybki dostęp</h2>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <a href="/events" className="p-4 border rounded-lg hover:bg-gray-50 transition-colors">
                  <h3 className="font-semibold mb-1">Szukaj Wydarzeń</h3>
                  <p className="text-sm text-gray-600">Przeglądaj nadchodzące wydarzenia</p>
                </a>
                <a href="/event-requests" className="p-4 border rounded-lg hover:bg-gray-50 transition-colors">
                  <h3 className="font-semibold mb-1">Moje Wnioski</h3>
                  <p className="text-sm text-gray-600">Zarządzaj swoimi wnioskami</p>
                </a>
                <a href="/reservations" className="p-4 border rounded-lg hover:bg-gray-50 transition-colors">
                  <h3 className="font-semibold mb-1">Rezerwacje Sprzętu</h3>
                  <p className="text-sm text-gray-600">Przeglądaj dostępny sprzęt</p>
                </a>
              </div>
            </div>

    

            <div className="p-4 border border-gray-200 rounded-lg bg-gray-50">
              <h3 className="font-semibold mb-3">Jak to działa:</h3>
              <ol className="space-y-2 text-sm text-gray-700">
                <li>1. Przeglądaj dostępne wydarzenia w sekcji Wydarzenia</li>
                <li>2. Złóż wniosek o dołączenie do interesującego Cię projektu</li>
                <li>3. Po złożeniu wniosku, możesz od razu zarezerwować potrzebny sprzęt</li>
                <li>4. Oczekuj na potwierdzenie od moderatora</li>
              </ol>
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}