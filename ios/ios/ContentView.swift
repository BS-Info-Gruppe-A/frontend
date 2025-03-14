import HausFix
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> some UIViewController {
        UIViewControllerKt.UIViewController()
    }

    func updateUIViewController(
        _ uiViewController: UIViewControllerType, context: Context
    ) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
