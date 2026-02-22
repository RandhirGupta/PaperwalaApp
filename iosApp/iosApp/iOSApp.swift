import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinInit.shared.doInit()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
